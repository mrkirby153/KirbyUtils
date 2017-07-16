package me.mrkirby153.kcutils;

import me.mrkirby153.kcutils.protocollib.ProtocolLib;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class ActionBar extends Module<JavaPlugin> implements Listener, Runnable {

    private ProtocolLib protocolLib;

    private HashMap<UUID, BaseComponent> actionBars = new HashMap<>();

    public ActionBar(JavaPlugin plugin) {
        super("actionbar", "1.0", plugin);
        protocolLib = new ProtocolLib(plugin);
    }

    /**
     * Clears a player's action bar
     *
     * @param player The player to clear the action bar for
     */
    public void clear(Player player) {
        if (protocolLib.isErrored())
            return;
        actionBars.remove(player.getUniqueId());
        protocolLib.sendActionBar(player, new TextComponent(""));
    }

    @EventHandler
    public void onLogout(PlayerQuitEvent event) {
        actionBars.remove(event.getPlayer().getUniqueId());
    }

    @Override
    public void run() {
        if (protocolLib.isErrored())
            return;
        List<UUID> toDelete = new ArrayList<>();
        actionBars.forEach((uuid, bar) -> {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) {
                toDelete.add(uuid);
                return;
            }
            protocolLib.sendActionBar(player, bar);
        });
        actionBars.entrySet().removeIf(entry -> toDelete.contains(entry.getKey()));
    }

    /**
     * Set the player's action bar
     *
     * @param player The player
     * @param text   The action bar to set
     */
    public void set(Player player, BaseComponent text) {
        if (protocolLib.isErrored())
            return;
        actionBars.put(player.getUniqueId(), text);
        protocolLib.sendActionBar(player, text);
    }

    @Override
    protected void init() {
        registerListener(this);
        scheduleRepeating(this, 0L, 5L);
    }
}
