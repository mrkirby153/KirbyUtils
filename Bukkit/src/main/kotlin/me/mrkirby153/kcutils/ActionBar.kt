package me.mrkirby153.kcutils

import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

/**
 * A module for handling the player's [ActionBar]
 *
 * @param plugin    The plugin that this module is a member of
 */
class ActionBar(plugin: JavaPlugin) : Module<JavaPlugin>("actionbar", plugin), Listener, Runnable {

    private val actionBars = HashMap<UUID, Component>()

    /**
     * Clears a player's action bar
     *
     * @param player The player to clear the action bar for
     */
    fun clear(player: Player) {
        actionBars.remove(player.uniqueId)
        player.sendActionBar(Component.empty())
    }

    @EventHandler
    fun onLogout(event: PlayerQuitEvent) {
        actionBars.remove(event.player.uniqueId)
    }

    override fun run() {
        val toDelete = ArrayList<UUID>()
        actionBars.forEach { (uuid, bar) ->
            val player = Bukkit.getPlayer(uuid)
            if (player == null) {
                toDelete.add(uuid)
            } else {
                player.sendActionBar(bar)
            }
        }
        actionBars.entries.removeIf { entry -> toDelete.contains(entry.key) }
    }

    /**
     * Set the player's action bar
     *
     * @param player The player
     * @param text   The action bar to set
     */
    operator fun set(player: Player, text: Component) {
        actionBars[player.uniqueId] = text
    }

    override fun init() {
        registerListener(this)
        scheduleRepeating(0L, 5L, this)
    }
}
