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
import java.util.*
import java.util.function.BiConsumer
import java.util.function.Consumer

/**
 * An abstract class of a gui
 *
 * @param <T> A JavaPlugin
</T> */
abstract class Gui<T : JavaPlugin>(protected var plugin: T, rows: Int, title: String) : Listener {

    private var actions: MutableMap<Int, BiConsumer<Player, ClickType>> = HashMap()
    var inventory: Inventory? = null
        private set

    private val onOpenEvents = ArrayList<Consumer<Player>>()

    /**
     * Adds a button (clickable item) to the GUI
     *
     * @param slot   The slot
     * @param item   The item to add
     * @param action The action to add
     */
    fun addButton(slot: Int, item: ItemStack, action: BiConsumer<Player, ClickType>) {
        actions.put(slot, action)
        inventory!!.setItem(slot, item)
    }

    abstract fun build()

    /**
     * Clears the inventory
     */
    fun clear() {
        this.actions.clear()
        this.inventory!!.clear()
    }

    fun inventoryClose() {
        HandlerList.unregisterAll(this)
        onClose()
    }

    fun onClose() {

    }

    @EventHandler(ignoreCancelled = true)
    fun onInventoryClick(event: InventoryClickEvent) {
        if (event.clickedInventory == null)
            return
        if (event.clickedInventory != inventory)
            return
        event.isCancelled = true
        val slot = event.slot
        val `is` = event.view.getItem(event.rawSlot)
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
            inventoryClose()
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
     * Gets the [Action][BiConsumer] in a slot
     *
     * @param slot The slot
     * @return The action
     */
    private fun getAction(slot: Int): BiConsumer<Player, ClickType>? {
        return actions[slot]
    }

    init {
        this.inventory = Bukkit.createInventory(null, rows * 9, title)
    }
}
