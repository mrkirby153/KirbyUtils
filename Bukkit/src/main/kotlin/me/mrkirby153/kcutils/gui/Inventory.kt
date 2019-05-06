package me.mrkirby153.kcutils.gui

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.HandlerList
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.event.player.PlayerDropItemEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.HashMap
import java.util.UUID
import java.util.function.BiConsumer

/**
 * An inventory gui using the player's current inventory
 *
 *
 */
abstract class Inventory<T : JavaPlugin>(protected var plugin: T, protected var player: Player) : Listener {
    private val actions = HashMap<ItemStack, BiConsumer<Player, ClickType>>()

    /**
     * Constructs the inventory
     */
    abstract fun build()

    /**
     * Clears the inventory
     */
    fun clear() {
        player.inventory.clear()
        actions.clear()
    }

    /**
     * Closes the inventory
     */
    fun close() {
        onClose()
        HandlerList.unregisterAll(this)
        openInventories.remove(player.uniqueId)
        player.inventory.clear()
        player.updateInventory()
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onInventoryClick(event: InventoryClickEvent) {
        val clickedInventory = event.clickedInventory ?: return
        val currentItem = event.currentItem ?: return
        if (event.whoClicked !== player)
            return
        if (clickedInventory.type != InventoryType.PLAYER)
            return
        val action = getAction(currentItem)
        action?.accept(event.whoClicked as Player, event.click)
        event.isCancelled = true
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerDropItem(event: PlayerDropItemEvent) {
        if (event.player === player)
            if (getAction(event.itemDrop.itemStack) != null)
                event.isCancelled = true
    }

    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        if (event.player !== player)
            return
        var clickType: ClickType? = null
        when (event.action) {
            Action.RIGHT_CLICK_BLOCK, Action.RIGHT_CLICK_AIR -> clickType = if (event.player.isSneaking)
                ClickType.SHIFT_RIGHT
            else
                ClickType.RIGHT
            Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> clickType = if (event.player.isSneaking)
                ClickType.SHIFT_LEFT
            else
                ClickType.LEFT
            else -> {
                ClickType.RIGHT
            }
        }
        if (clickType == null)
            return
        val item = event.item ?: return
        event.isCancelled = true
        val action = getAction(item)
        action?.accept(event.player, clickType)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        if (event.player !== player)
            return
        close()
    }

    /**
     * Opens the inventory
     */
    fun open() {
        if (openInventories[player.uniqueId] != null) {
            openInventories[player.uniqueId]?.close()
        }
        onOpen()
        build()
        openInventories.put(player.uniqueId, this)
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * Gets an [ItemStack&#39;s][ItemStack] action
     *
     * @param item The item
     * @return The action
     */
    private fun getAction(item: ItemStack): BiConsumer<Player, ClickType>? {
        return actions[item]
    }

    /**
     * Adds an item to the player's inventory
     *
     * @param slot   The slot to add
     * @param stack  The item to add
     * @param action The optional action to add
     */
    protected fun addItem(slot: Int, stack: ItemStack, action: BiConsumer<Player, ClickType>) {
        actions.put(stack, action)
        player.inventory.setItem(slot, stack)
        player.updateInventory()
    }

    /**
     * Gets a hotbar slot
     *
     * @param slot The slot
     * @return The hotbar slot
     */
    protected fun hotbarSlot(slot: Int): Int {
        var slot = slot
        return --slot
    }

    /**
     * Converts a row and column into a raw slot
     *
     * @param row The row
     * @param col The column
     * @return The raw slot
     */
    protected fun invRowCol(row: Int, col: Int): Int {
        if (row < MIN_ROWS || row > MAX_ROWS)
            throw IllegalArgumentException("There are only 3 rows in a player inventory!")
        if (col < MIN_COLUMNS || col > MAX_COLUMNS)
            throw IllegalArgumentException("There are only 9 columns in a player inventory!")
        var slot = (row - ROW_OFFSET) * COLUMNS_PER_ROW
        slot += col - COLUMN_OFFSET
        return slot + PLAYER_INV_START_SLOT
    }

    /**
     * Called when the inventory "closes"
     */
    protected fun onClose() {

    }

    /**
     * Called when the inventory "opens"
     */
    protected fun onOpen() {

    }

    companion object {

        private const val MIN_ROWS = 1
        private const val MIN_COLUMNS = 1
        private const val MAX_ROWS = 3
        private const val MAX_COLUMNS = 9
        private const val ROW_OFFSET = 1
        private const val COLUMN_OFFSET = 1
        private const val COLUMNS_PER_ROW = 9
        private const val PLAYER_INV_START_SLOT = 9
        private val openInventories = HashMap<UUID, Inventory<out JavaPlugin>>()

        /**
         * Gets the player's currently open inventory
         *
         * @param player The player
         * @return The inventory`
         */
        fun getOpenInventory(player: Player): Inventory<out JavaPlugin>? {
            return openInventories[player.uniqueId]
        }
    }
}
