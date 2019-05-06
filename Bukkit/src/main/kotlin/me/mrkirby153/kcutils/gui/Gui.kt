package me.mrkirby153.kcutils.gui

import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.lang.ref.WeakReference
import java.util.HashMap
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * An abstract class of a gui
 *
 * @param <T> A JavaPlugin
*/
abstract class Gui<T : JavaPlugin>(protected var plugin: T, rows: Int, title: String) : Listener {

    /**
     * The map of slots to their actions
     */
    private var actions: MutableMap<Int, BiConsumer<Player, ClickType>> = HashMap()

    /**
     * The inventory
     */
    protected val inventory: Inventory = Bukkit.createInventory(null, rows * 9)

    /**
     * If the listener has been registered
     */
    var listenerRegistered = false

    /**
     * Events fired when the inventory is opened for a player
     */
    private val onOpenEvents = ArrayList<Consumer<Player>>()

    /**
     * Events fired when the inventory is closed for a player
     */
    private val onCloseEvents = ArrayList<Consumer<Player>>()

    /**
     * A list of players who currently have the GUI open
     */
    private val observers = mutableListOf<WeakReference<Player>>()

    /**
     * Adds a button (clickable item) to the GUI
     *
     * @param slot   The slot
     * @param item   The item to add
     * @param action The action to add
     */
    fun addButton(slot: Int, item: ItemStack, action: BiConsumer<Player, ClickType>) {
        actions.put(slot, action)
        inventory.setItem(slot, item)
    }

    /**
     * Constructs the inventory for displaying
     */
    abstract fun build()

    /**
     * Clears the inventory
     */
    fun clear() {
        this.actions.clear()
        this.inventory.clear()
    }

    private fun inventoryClose(player: Player) {
        onCloseEvents.forEach { it.accept(player) }
        observers.removeIf {
            val p = it.get()
            p != null && p == player
        }
        // Unregister the listener only if there are no more observers
        if (observers.none { it.get() != null }) {
            HandlerList.unregisterAll(this)
            listenerRegistered = false
        }
    }

    @Deprecated("")
    fun onClose() {

    }

    /**
     * Registers a close event consumer
     *
     * @param consumer The consumer to register
     */
    fun onClose(consumer: Consumer<Player>) {
        this.onCloseEvents.add(consumer)
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory == null)
            return
        if (event.clickedInventory != inventory)
            return
        event.isCancelled = true
        val slot = event.slot
        val `is` = event.view.getItem(event.rawSlot) ?: return
        if (`is`.type == Material.AIR)
            actions.remove(slot)
        val consumer = getAction(slot)
        if (consumer != null && event.whoClicked is Player) {
            consumer.accept(event.whoClicked as Player, event.click)
        }
    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClose(event: InventoryCloseEvent) {
        if (event.inventory == this.inventory)
            inventoryClose(event.player as Player)
    }

    @Deprecated("")
    fun onOpen() {

    }

    /**
     * Add an event to be run when the inventory opens
     *
     * @param consumer The consumer
     */
    fun onOpen(consumer: Consumer<Player>) {
        this.onOpenEvents.add(consumer)
    }

    /**
     * Opens the inventory
     *
     * @param player The player to open the inventory for
     */
    fun open(player: Player) {
        onOpenEvents.forEach { e -> e.accept(player) }
        if(!listenerRegistered){
            Bukkit.getServer().pluginManager.registerEvents(this, plugin)
            listenerRegistered = true
        }
        build()
        player.openInventory(inventory)
    }

    /**
     * Rebuilds the inventory
     */
    fun rebuild() {
        clear()
        build()
    }

    /**
     * Gets a list of players observing the inventory
     */
    fun getObservers(): List<Player> = this.observers.filter { it.get() != null }.map { it.get()!! }

    /**
     * Gets the [Action][BiConsumer] in a slot
     *
     * @param slot The slot
     * @return The action
     */
    private fun getAction(slot: Int): BiConsumer<Player, ClickType>? {
        return actions[slot]
    }
}
