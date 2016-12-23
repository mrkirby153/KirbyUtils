package me.mrkirby153.kcutils.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;

/**
 * An abstract class of a gui
 *
 * @param <T> A JavaPlugin
 */
public abstract class Gui<T extends JavaPlugin> implements Listener {

    protected T plugin;
    protected Map<Integer, Action> actions = new HashMap<>();
    private Inventory inventory;
    private Player player;

    public Gui(T plugin, Player player, int rows, String title) {
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.player = player;
        this.plugin = plugin;
    }

    public abstract void build();

    /**
     * Closes the inventory
     */
    public void close() {
        player.closeInventory();
    }

    public Inventory getInventory() {
        return inventory;
    }

    public Player getPlayer() {
        return player;
    }

    public final void inventoryClose() {
        HandlerList.unregisterAll(this);
        onClose();
    }

    public void onClose() {

    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (event.getClickedInventory() == null)
            return;
        if (!event.getClickedInventory().equals(getInventory()))
            return;
        event.setCancelled(true);
        int slot = event.getSlot();
        ItemStack is = event.getView().getItem(event.getRawSlot());
        if (is.getType() == Material.AIR)
            actions.remove(slot);
        Action a = getAction(slot);
        if (a != null && event.getWhoClicked() instanceof Player) {
            a.onClick((Player) event.getWhoClicked(), event.getClick());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if(event.getInventory().equals(this.inventory))
            inventoryClose();
    }

    public void onOpen() {

    }

    /**
     * Opens the inventory for the player
     */
    public void open() {
        onOpen();
        build();
        player.openInventory(getInventory());
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Gets the {@link Action} in a slot
     *
     * @param slot The slot
     * @return The action
     */
    private Action getAction(int slot) {
        return actions.get(slot);
    }

    /**
     * Adds a button (clickable item) to the GUI
     *
     * @param slot   The slot
     * @param item   The item to add
     * @param action The action to add
     */
    protected final void addButton(int slot, ItemStack item, Action action) {
        actions.put(slot, action);
        getInventory().setItem(slot, item);
    }
}
