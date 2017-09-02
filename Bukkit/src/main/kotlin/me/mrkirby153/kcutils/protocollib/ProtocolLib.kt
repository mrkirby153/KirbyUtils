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
     * Sends text to the player's Action Bar
     *
     * @param player    The player to send the text to
     * @param component The text to send
     */
    fun sendActionBar(player: Player, component: BaseComponent) {
        val actionBar = protocolManager!!.createPacket(PacketType.Play.Server.CHAT)
        try {
            actionBar.chatTypes.write(0, EnumWrappers.ChatType.GAME_INFO)
        } catch (e: Exception) {
            // Ignore
        }

        try {
            actionBar.bytes.write(0, 2.toByte())
        } catch (e: Exception) {
            // Ignore
        }

        actionBar.chatComponents.write(0, WrappedChatComponent.fromText(component.toLegacyText()))
        sendPacket(player, actionBar)
    }

    /**
     * Sends a ProtocolLib [PacketContainer] to a player
     *
     * @param player The player to send the packet to
     * @param packet The [PacketContainer] to send
     */
    fun sendPacket(player: Player, packet: PacketContainer) {
        try {
            protocolManager!!.sendServerPacket(player, packet)
        } catch (e: InvocationTargetException) {
            Throwables.propagate(e)
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

    /**
     * Sends a title to a player with custom timings
     *
     * @param player   The player to send the title to
     * @param title    The title to send
     * @param subtitle The subtitle to send
     * @param timings  The [timings][TitleTimings] to send
     */
    @JvmOverloads
    fun title(player: Player, title: String, subtitle: String, timings: TitleTimings = TitleTimings(20, 20, 20)) {
        sendPackets(player, *constructTitle(title, subtitle, timings).toTypedArray())
    }

    /**
     * Constructs a series of packets to display a Title to the player
     *
     * @param title    The title
     * @param subtitle The subtitle
     * @param timings  The timings for the title/subtitle
     * @return A list of packets representing the title
     */
    private fun constructTitle(title: String, subtitle: String?, timings: TitleTimings): List<PacketContainer> {
        val packets = ArrayList<PacketContainer>()

        val timingsPacket = protocolManager!!.createPacket(PacketType.Play.Server.TITLE)
        timingsPacket.titleActions.write(0, EnumWrappers.TitleAction.TIMES)

        timingsPacket.integers.write(0, timings.fadeInTicks)
        timingsPacket.integers.write(1, timings.stayTicks)
        timingsPacket.integers.write(2, timings.fadeOutTicks)

        packets.add(timingsPacket)


        val titlePacket = protocolManager!!.createPacket(PacketType.Play.Server.TITLE)
        titlePacket.modifier.writeDefaults()

        titlePacket.titleActions.write(0, EnumWrappers.TitleAction.TITLE)
        titlePacket.chatComponents.write(0, WrappedChatComponent.fromText(title))
        packets.add(titlePacket)

        if (subtitle != null) {
            val subtitlePacket = protocolManager!!.createPacket(PacketType.Play.Server.TITLE)
            subtitlePacket.modifier.writeDefaults()

            subtitlePacket.titleActions.write(0, EnumWrappers.TitleAction.SUBTITLE)
            subtitlePacket.chatComponents.write(0, WrappedChatComponent.fromText(subtitle))

            packets.add(subtitlePacket)
        }
        return packets
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

data class TitleTimings(val fadeInTicks: Int, val stayTicks: Int, val fadeOutTicks: Int)