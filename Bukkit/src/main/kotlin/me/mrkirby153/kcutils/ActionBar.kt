package me.mrkirby153.kcutils

import me.mrkirby153.kcutils.protocollib.ProtocolLib
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class ActionBar(plugin: JavaPlugin) : Module<JavaPlugin>("actionbar", plugin), Listener, Runnable {

    private val protocolLib: ProtocolLib = ProtocolLib(plugin)

    private val actionBars = HashMap<UUID, BaseComponent>()

    /**
     * Clears a player's action bar
     *
     * @param player The player to clear the action bar for
     */
    fun clear(player: Player) {
        if (protocolLib.isErrored)
            return
        actionBars.remove(player.uniqueId)
        protocolLib.sendActionBar(player, TextComponent(""))
    }

    @EventHandler
    fun onLogout(event: PlayerQuitEvent) {
        actionBars.remove(event.player.uniqueId)
    }

    override fun run() {
        if (protocolLib.isErrored)
            return
        val toDelete = ArrayList<UUID>()
        actionBars.forEach { uuid, bar ->
            val player = Bukkit.getPlayer(uuid)
            if (player == null) {
                toDelete.add(uuid)
            } else {
                protocolLib.sendActionBar(player, bar)
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
    operator fun set(player: Player, text: BaseComponent) {
        if (protocolLib.isErrored)
            return
        actionBars.put(player.uniqueId, text)
        protocolLib.sendActionBar(player, text)
    }

    override fun init() {
        protocolLib.init()
        registerListener(this)
        scheduleRepeating(0L, 5L, this)
    }
}
