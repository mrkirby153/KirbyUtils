package me.mrkirby153.kcutils.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * An abstract class of a gui
 *
 * @param <T> A JavaPlugin
 */
public abstract class Gui<T extends JavaPlugin> implements Listener {

    protected T plugin;
    protected Map<Integer, BiConsumer<Player, ClickType>> actions = new HashMap<>();
    private Inventory inventory;
    private Player player;

    private List<Consumer<Player>> onOpenEvents = new ArrayList<>();

    @Deprecated
    public Gui(T plugin, Player player, int rows, String title) {
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.player = player;
        this.plugin = plugin;
    }

    public Gui(T plugin, int rows, String title) {
        this.inventory = Bukkit.createInventory(null, rows * 9, title);
        this.plugin = plugin;
    }

    /**
     * Adds a button (clickable item) to the GUI
     *
     * @param slot   The slot
     * @param item   The item to add
     * @param action The action to add
     */
    public final void addButton(int slot, ItemStack item, BiConsumer<Player, ClickType> action) {
        actions.put(slot, action);
        getInventory().setItem(slot, item);
    }

    public abstract void build();

    /**
     * Clears the inventory
     */
    public void clear() {
        this.actions.clear();
        this.inventory.clear();
    }

    /**
     * Closes the inventory
     */
    @Deprecated
    public void close() {
        player.closeInventory();
    }

    public Inventory getInventory() {
        return inventory;
    }

    @Deprecated
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
        BiConsumer<Player, ClickType> consumer = getAction(slot);
        if (consumer != null && event.getWhoClicked() instanceof Player) {
            consumer.accept((Player) event.getWhoClicked(), event.getClick());
        }
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().equals(this.inventory))
            inventoryClose();
    }

    @Deprecated
    public void onOpen() {

    }

    /**
     * Add an event to be run when the inventory opens
     *
     * @param consumer The consumer
     */
    public void onOpen(Consumer<Player> consumer) {
        this.onOpenEvents.add(consumer);
    }

    /**
     * Opens the inventory for the player
     */
    @Deprecated
    public void open() {
        if (player != null) {
            onOpen();
            this.onOpenEvents.forEach(c -> c.accept(player));
            build();
            player.openInventory(getInventory());
            plugin.getServer().getPluginManager().registerEvents(this, plugin);
        } else {
            throw new IllegalStateException("Using old api. Call open(Player)");
        }
    }

    /**
     * Opens the inventory
     *
     * @param player The player to open the inventory for
     */
    public void open(Player player) {
        onOpenEvents.forEach(e -> e.accept(player));
        build();
        player.openInventory(getInventory());
    }

    /**
     * Rebuilds the inventory
     */
    public void rebuild() {
        clear();
        build();
    }

    /**
     * Gets the {@link BiConsumer Action} in a slot
     *
     * @param slot The slot
     * @return The action
     */
    private BiConsumer<Player, ClickType> getAction(int slot) {
        return actions.get(slot);
    }
}
