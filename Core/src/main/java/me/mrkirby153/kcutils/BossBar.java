package me.mrkirby153.kcutils;

import org.bukkit.Bukkit;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

/**
 * A class for manipulating boss bars
 */
public class BossBar extends Module<JavaPlugin> implements Listener {

    private static HashMap<UUID, org.bukkit.boss.BossBar> playerBossBars = new HashMap<>();

    public BossBar(JavaPlugin plugin) {
        super("bossbar", "1.0", plugin);
    }

    public static org.bukkit.boss.BossBar getBar(Player player) {
        return playerBossBars.get(player.getUniqueId());
    }


    @EventHandler(ignoreCancelled = true)
    public void onPlayerQuit(PlayerQuitEvent event) {
        playerBossBars.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Remove a boss bar from the player
     *
     * @param player The player
     */
    public void remove(Player player) {
        org.bukkit.boss.BossBar bar = playerBossBars.remove(player.getUniqueId());
        if (bar == null)
            return;
        bar.removeAll();
    }

    /**
     * Remove all online player's boss bars
     */
    public void removeAll() {
        Bukkit.getOnlinePlayers().forEach(this::remove);
    }

    /**
     * Sets a player's boss bar color
     *
     * @param player The player
     * @param color  The color of the bar
     */
    public void setColor(Player player, BarColor color) {
        org.bukkit.boss.BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar == null)
            return;
        bar.setColor(color);
    }

    /**
     * Set the boss bar's progress
     *
     * @param player  The player
     * @param percent The percent fill of the boss bar
     */
    public void setPercent(Player player, double percent) {
        org.bukkit.boss.BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar == null)
            return;
        if(percent < 0)
            percent = 0;
        if(percent > 1)
            percent = 1;
        bar.setProgress(percent);
    }

    /**
     * Set the boss bar's style
     *
     * @param player The player
     * @param style  The boss bar's style
     */
    public void setStyle(Player player, BarStyle style) {
        org.bukkit.boss.BossBar bar = playerBossBars.get(player.getUniqueId());
        if (bar == null)
            return;
        bar.setStyle(style);
    }

    /**
     * Set the boss bar's text
     *
     * @param player The player
     * @param text   The text
     */
    public void setTitle(Player player, String text) {
        org.bukkit.boss.BossBar bar = playerBossBars.get(player.getUniqueId());
        if ((text == null || text.isEmpty()) && bar != null) {
            bar.removeAll();
            playerBossBars.remove(player.getUniqueId());
            return;
        }
        if (bar == null) {
            playerBossBars.put(player.getUniqueId(), bar = Bukkit.createBossBar(text, BarColor.PINK, BarStyle.SOLID));
            bar.addPlayer(player);
        }
        bar.setTitle(text);
    }

    @Override
    protected void init() {
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
    }
}
