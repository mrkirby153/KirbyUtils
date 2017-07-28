package me.mrkirby153.kcutils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiConsumer;

/**
 * An inventory gui using the player's current inventory
 *
 * @param <T>
 */
public abstract class Inventory<T extends JavaPlugin> implements Listener {

    private static final int MIN_ROWS = 1;
    private static final int MIN_COLUMNS = 1;
    private static final int MAX_ROWS = 3;
    private static final int MAX_COLUMNS = 9;
    private static final int ROW_OFFSET = 1;
    private static final int COLUMN_OFFSET = 1;
    private static final int COLUMNS_PER_ROW = 9;
    private static final int PLAYER_INV_START_SLOT = 9;
    private static HashMap<UUID, Inventory<? extends JavaPlugin>> openInventories = new HashMap<>();
    protected T plugin;
    protected Player player;
    private Map<ItemStack, BiConsumer<Player, ClickType>> actions = new HashMap<>();

    public Inventory(T plugin, Player player) {
        this.player = player;
        this.plugin = plugin;
    }

    /**
     * Gets the player's currently open inventory
     *
     * @param player The player
     * @return The inventory`
     */
    public static Inventory<? extends JavaPlugin> getOpenInventory(Player player) {
        return openInventories.get(player.getUniqueId());
    }

    /**
     * Constructs the inventory
     */
    public abstract void build();

    /**
     * Clears the inventory
     */
    public void clear() {
        player.getInventory().clear();
        actions.clear();
    }

    /**
     * Closes the inventory
     */
    public void close() {
        onClose();
        HandlerList.unregisterAll(this);
        openInventories.remove(player.getUniqueId());
        player.getInventory().clear();
        player.updateInventory();
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() != player)
            return;
        if (event.getClickedInventory() == null || event.getClickedInventory().getType() != InventoryType.PLAYER)
            return;
        if (event.getCurrentItem() == null)
            return;
        BiConsumer<Player, ClickType> action = getAction(event.getCurrentItem());
        if (action != null) {
            action.accept((Player) event.getWhoClicked(), event.getClick());
        }
        event.setCancelled(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerDropItem(PlayerDropItemEvent event) {
        if (event.getPlayer() == player)
            if (getAction(event.getItemDrop().getItemStack()) != null)
                event.setCancelled(true);
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getPlayer() != player)
            return;
        ClickType clickType = null;
        switch (event.getAction()) {
            case RIGHT_CLICK_BLOCK:
            case RIGHT_CLICK_AIR:
                if (event.getPlayer().isSneaking())
                    clickType = ClickType.SHIFT_RIGHT;
                else
                    clickType = ClickType.RIGHT;
                break;
            case LEFT_CLICK_AIR:
            case LEFT_CLICK_BLOCK:
                if (event.getPlayer().isSneaking())
                    clickType = ClickType.SHIFT_LEFT;
                else
                    clickType = ClickType.LEFT;
                break;
        }
        if (clickType == null || event.getItem() == null)
            return;
        event.setCancelled(true);
        BiConsumer<Player, ClickType> action = getAction(event.getItem());
        if (action != null)
            action.accept(event.getPlayer(), clickType);
    }

    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (event.getPlayer() != player)
            return;
        close();
    }

    /**
     * Opens the inventory
     */
    public void open() {
        if(openInventories.get(player.getUniqueId()) != null){
            openInventories.get(player.getUniqueId()).close();
        }
        onOpen();
        build();
        openInventories.put(player.getUniqueId(), this);
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Gets an {@link ItemStack ItemStack's} action
     *
     * @param item The item
     * @return The action
     */
    private BiConsumer<Player, ClickType> getAction(ItemStack item) {
        return actions.get(item);
    }

    /**
     * Adds an item to the player's inventory
     *
     * @param slot   The slot to add
     * @param stack  The item to add
     * @param action The optional action to add
     */
    protected void addItem(int slot, ItemStack stack, BiConsumer<Player, ClickType> action) {
        actions.put(stack, action);
        player.getInventory().setItem(slot, stack);
        player.updateInventory();
    }

    /**
     * Gets a hotbar slot
     *
     * @param slot The slot
     * @return The hotbar slot
     */
    protected int hotbarSlot(int slot) {
        return --slot;
    }

    /**
     * Converts a row and column into a raw slot
     *
     * @param row The row
     * @param col The column
     * @return The raw slot
     */
    protected int invRowCol(int row, int col) {
        if (row < MIN_ROWS || row > MAX_ROWS)
            throw new IllegalArgumentException("There are only 3 rows in a player inventory!");
        if (col < MIN_COLUMNS || col > MAX_COLUMNS)
            throw new IllegalArgumentException("There are only 9 columns in a player inventory!");
        int slot = ((row - ROW_OFFSET) * COLUMNS_PER_ROW);
        slot += (col - COLUMN_OFFSET);
        return slot + PLAYER_INV_START_SLOT;
    }

    /**
     * Called when the inventory "closes"
     */
    protected void onClose() {

    }

    /**
     * Called when the inventory "opens"
     */
    protected void onOpen() {

    }
}
