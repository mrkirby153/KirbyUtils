package me.mrkirby153.kcutils.gui

import me.mrkirby153.kcutils.extensions.runnable
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import java.lang.ref.WeakReference
import java.util.UUID


@DslMarker
internal annotation class GuiDslMarker

/**
 * Creates a new gui with the specified number of [rows] and the provided [title]
 */
fun <T : Plugin> T.gui(rows: Int, title: Component, body: Gui<T>.() -> Unit) =
    Gui(this, rows, title).apply(body)


typealias GuiOnOpenHandler = (Player) -> Unit
typealias GuiCanOpenCheck = (Player) -> Boolean
typealias GuiCanUpdateHandler = () -> Unit

/**
 * A gui is an interactive inventory that users can click to perform various actions
 */
class Gui<T : Plugin>(
    protected var plugin: T,
    private val rows: Int,
    private val title: Component
) : Listener {
    private var slots = mutableMapOf<Int, Slot>()

    private val inventories = mutableMapOf<UUID, Inventory>()
    private var observers = mutableListOf<WeakReference<Player>>()

    private var onOpenHandler: GuiOnOpenHandler = {}
    private var canOpenHandler: GuiCanOpenCheck = { true }
    private var onUpdateHandler: GuiCanUpdateHandler = {}

    private var initialized = false

    private var updateTask: BukkitTask? = null

    /**
     * The frequency at which the GUI will update in ticks. Set to `0` to disable updating
     */
    var updateFrequency: Long = 0
        set(value) {
            field = value
            updateTask?.cancel()
            updateTask = null
            scheduleUpdate()
        }

    /**
     * Adds a new slot at the given [row] and [slot] to this GUI. An [item] can be specified to
     * render the item in the slot
     */
    @GuiDslMarker
    fun slot(row: Int, slot: Int, item: ItemStack?, body: Slot.() -> Unit = {}) {
        val rawSlot = row * 9 + slot
        check(rawSlot < rows * 9) { "Attempting to access a row/slot that is outside of this inventory" }
        this.slots[rawSlot] = Slot(rawSlot, item, this).apply(body)
    }

    /**
     * Check to determine if this inventory can be opened
     */
    @GuiDslMarker
    fun onOpen(handler: GuiOnOpenHandler) {
        this.onOpenHandler = handler
    }

    /**
     * Callback invoked when a player opens an inventory
     */
    @GuiDslMarker
    fun canOpen(check: GuiCanOpenCheck) {
        this.canOpenHandler = check
    }

    /**
     * Callback called every [updateFrequency] ticks while the inventory is open
     */
    @GuiDslMarker
    fun onUpdate(handler: GuiCanUpdateHandler) {
        this.onUpdateHandler = handler
    }

    /**
     * Opens this inventory for the provided [player]
     */
    fun open(player: Player) {
        if (!this.canOpenHandler.invoke(player))
            return
        initialize()
        if (this.observers.none { it.get()?.uniqueId == player.uniqueId })
            this.observers.add(WeakReference(player))
        render(player)
        player.openInventory(getInventoryForPlayer(player))
        this.onOpenHandler.invoke(player)
    }

    /**
     * Closes this inventory for the provided [player]
     */
    fun close(player: Player) {
        player.closeInventory(InventoryCloseEvent.Reason.PLUGIN)
    }

    @EventHandler(priority = EventPriority.MONITOR)
    fun onClose(event: InventoryCloseEvent) {
        handleClose(event.player as Player)
    }

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val inventory = event.clickedInventory ?: return
        val player = event.whoClicked as? Player ?: return
        if (inventory != getInventoryForPlayer(player)) {
            return
        }
        val slotNum = event.slot
        val slot = this.slots[slotNum] ?: return
        event.isCancelled = slot.fireOnClick(this, slot, inventory, player, event.click)
    }

    /**
     * Re-renders the given [player]'s inventory
     */
    fun refresh(player: Player) {
        render(player)
    }

    private fun render(player: Player) {
        val inventory = getInventoryForPlayer(player)
        this.slots.forEach { (id, slot) ->
            var item = slot.item?.clone()
            item = slot.fireRender(this, slot, inventory, player, item).item
            inventory.setItem(id, item)
        }
    }

    private fun handleClose(player: Player) {
        observers.removeIf {
            it.get() == null || it.get()?.uniqueId == player.uniqueId
        }
        inventories.remove(player.uniqueId)
        // Nobody else is looking at this, clean up some resources
        if (observers.isEmpty()) {
            HandlerList.unregisterAll(this)
            updateTask?.cancel()
            updateTask = null
        }
    }

    private fun scheduleUpdate() {
        if (updateFrequency > 0 && updateTask == null) {
            runnable { update() }.runTaskTimer(plugin, 0, updateFrequency)
        }
    }

    private fun update() {
        observers.mapNotNull { it.get() }.forEach { player ->
            val inventory = getInventoryForPlayer(player)
            this.slots.forEach { (id, slot) ->
                var item = slot.item?.clone()
                item = slot.fireUpdate(this, slot, inventory, player, item).item
                inventory.setItem(id, item)
            }
        }
    }

    private fun initialize() {
        if (initialized) {
            return
        }
        scheduleUpdate()
        Bukkit.getServer().pluginManager.registerEvents(this, plugin)
    }

    private fun getInventoryForPlayer(player: Player) =
        inventories.computeIfAbsent(player.uniqueId) {
            Bukkit.createInventory(null, rows * 9, title)
        }

}


typealias SlotClickHandler = SlotClick.() -> Unit
typealias SlotRenderHandler = SlotRender.() -> Unit
typealias SlotUpdateHandler = SlotUpdate.() -> Unit

/**
 * A slot represents a slot in the inventory. It holds various callbacks that are invoked throughout
 * the GUI's lifecycle and when the user interacts with the inventory
 */
class Slot(
    private val rawSlot: Int,
    internal val item: ItemStack?,
    private val gui: Gui<*>
) {

    private var renderHandler: SlotRenderHandler = {}
    private var updateHandler: SlotUpdateHandler = {}
    private var clickHandler: SlotClickHandler = {}

    /**
     * Callback invoked when the gui is rendered
     */
    @GuiDslMarker
    fun onRender(handler: SlotRenderHandler) {
        this.renderHandler = handler
    }

    /**
     * Callback invoked when this slot is clicked on
     */
    @GuiDslMarker
    fun onClick(handler: SlotClickHandler) {
        this.clickHandler = handler
    }

    /**
     * Callback invoked periodically according to [Gui.updateFrequency] of the owning inventory
     */
    @GuiDslMarker
    fun onUpdate(handler: SlotUpdateHandler) {
        this.updateHandler = handler
    }

    internal fun fireRender(
        gui: Gui<*>,
        slot: Slot,
        inventory: Inventory,
        player: Player,
        item: ItemStack?
    ): SlotRender {
        val row = slot.rawSlot.floorDiv(9)
        val col = slot.rawSlot.mod(9)
        return SlotRender(gui, row, col, inventory, player, item).apply(renderHandler)
    }

    internal fun fireUpdate(
        gui: Gui<*>,
        slot: Slot,
        inventory: Inventory,
        player: Player,
        item: ItemStack?
    ): SlotUpdate {
        val row = slot.rawSlot.floorDiv(9)
        val col = slot.rawSlot.mod(9)
        return SlotUpdate(gui, row, col, inventory, player, item).apply(updateHandler)
    }

    internal fun fireOnClick(
        gui: Gui<*>,
        slot: Slot,
        inventory: Inventory,
        player: Player,
        clickType: ClickType
    ): Boolean {
        val row = slot.rawSlot.floorDiv(9)
        val col = slot.rawSlot.mod(9)
        return SlotClick(gui, inventory, row, col, clickType = clickType, player = player).apply(
            clickHandler
        ).shouldCancel
    }
}

/**
 * Data class for handling the periodic slot update events
 */
data class SlotUpdate(
    val gui: Gui<*>,
    val row: Int,
    val col: Int,
    val inventory: Inventory,
    val player: Player,
    var item: ItemStack?
)

/**
 * Data class for handling the slot render event
 */
data class SlotRender(
    val gui: Gui<*>,
    val row: Int,
    val col: Int,
    val inventory: Inventory,
    val player: Player,
    var item: ItemStack?
)

/**
 * Data class for handling the slot click event
 */
data class SlotClick(
    val gui: Gui<*>,
    val inventory: Inventory,
    val row: Int,
    val col: Int,
    var shouldCancel: Boolean = true,
    val clickType: ClickType,
    val player: Player
) {

    /**
     * Close the GUI for the player
     */
    fun close() {
        gui.close(player)
    }

    /**
     * Refresh the GUI for the player
     */
    fun refresh() {
        gui.refresh(player)
    }
}