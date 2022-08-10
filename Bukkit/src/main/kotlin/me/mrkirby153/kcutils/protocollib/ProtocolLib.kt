package me.mrkirby153.kcutils.protocollib

import com.comphenix.protocol.PacketType
import com.comphenix.protocol.ProtocolLibrary
import com.comphenix.protocol.ProtocolManager
import com.comphenix.protocol.events.PacketContainer
import com.comphenix.protocol.wrappers.EnumWrappers
import com.comphenix.protocol.wrappers.WrappedChatComponent
import com.google.common.base.Throwables
import me.mrkirby153.kcutils.Module
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.lang.reflect.InvocationTargetException
import java.util.*

class ProtocolLib(plugin: JavaPlugin) : Module<JavaPlugin>("ProtocolLib", plugin) {

    private var protocolManager: ProtocolManager? = null

    /**
     * Gets if there was an issue initializing the ProtocolLib handler
     *
     * @return True if there was an error
     */
    var isErrored = false
        private set

    /**
     * Sends a ProtocolLib [PacketContainer] to a player
     *
     * @param player The player to send the packet to
     * @param packet The [PacketContainer] to send
     */
    fun sendPacket(player: Player, packet: PacketContainer) {
        if(isErrored) {
            return
        }
        try {
            protocolManager!!.sendServerPacket(player, packet)
        } catch (e: InvocationTargetException) {
            Throwables.throwIfUnchecked(e)
        }
    }

    /**
     * Sends multiple packets to a given player
     *
     * @param player  The player to send packets to
     * @param packets The packets to send
     */
    fun sendPackets(player: Player, vararg packets: PacketContainer) {
        Arrays.stream(packets).forEach { packet -> sendPacket(player, packet) }
    }

    override fun init() {
        if (plugin.server.pluginManager.getPlugin("ProtocolLib") == null) {
            log("ProtocolLib is not installed or not loaded. This module will not work correctly!")
            isErrored = true
            return
        }
        this.protocolManager = ProtocolLibrary.getProtocolManager()
    }
}