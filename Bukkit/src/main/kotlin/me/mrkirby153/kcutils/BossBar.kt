package me.mrkirby153.kcutils

import org.bukkit.Bukkit
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * A class for manipulating boss bars
 */
class BossBar(plugin: JavaPlugin) : Module<JavaPlugin>("bossbar", plugin), Listener {


    @EventHandler(ignoreCancelled = true)
    fun onPlayerQuit(event: PlayerQuitEvent) {
        playerBossBars.remove(event.player.uniqueId)
    }

    /**
     * Remove a boss bar from the player
     *
     * @param player The player
     */
    fun remove(player: Player) {
        val bar = playerBossBars.remove(player.uniqueId) ?: return
        bar.removeAll()
    }

    /**
     * Remove all online player's boss bars
     */
    fun removeAll() {
        Bukkit.getOnlinePlayers().forEach {
            remove(it)
        }
    }

    /**
     * Sets a player's boss bar color
     *
     * @param player The player
     * @param color  The color of the bar
     */
    fun setColor(player: Player, color: BarColor) {
        val bar = playerBossBars[player.uniqueId] ?: return
        bar.color = color
    }

    /**
     * Set the boss bar's progress
     *
     * @param player  The player
     * @param percent The percent fill of the boss bar
     */
    fun setPercent(player: Player, percent: Double) {
        val bar = playerBossBars[player.uniqueId] ?: return
        var prec = percent
        if (percent < 0)
            prec = 0.0
        if (percent > 1)
            prec = 1.0
        bar.progress = prec
    }

    /**
     * Set the boss bar's style
     *
     * @param player The player
     * @param style  The boss bar's style
     */
    fun setStyle(player: Player, style: BarStyle) {
        val bar = playerBossBars[player.uniqueId] ?: return
        bar.style = style
    }

    /**
     * Set the boss bar's text
     *
     * @param player The player
     * @param text   The text
     */
    fun setTitle(player: Player, text: String?) {
        var bar: org.bukkit.boss.BossBar? = playerBossBars[player.uniqueId]
        if ((text == null || text.isEmpty()) && bar != null) {
            bar.removeAll()
            playerBossBars.remove(player.uniqueId)
            return
        }
        if (bar == null) {
            bar = Bukkit.createBossBar(text, BarColor.PINK, BarStyle.SOLID)
            playerBossBars.put(player.uniqueId, bar)
            bar!!.addPlayer(player)
        }
        bar.title = text
    }

    override fun init() {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    companion object {

        private val playerBossBars = HashMap<UUID, org.bukkit.boss.BossBar>()

        /**
         * Gets the boss bars for the player
         *
         * @param player The boss bar for the player
         */
        fun getBar(player: Player): org.bukkit.boss.BossBar? {
            return playerBossBars[player.uniqueId]
        }
    }
}
