package me.mrkirby153.kcutils.reflections

import org.bukkit.Bukkit
import org.bukkit.entity.Player


object Reflections {

    val nmsVersion: String
        get() {
            return Bukkit.getServer().javaClass.`package`.name.replace(".", ",").split(
                    ",")[3]
        }

    /**
     * Gets a NMS class
     *
     * @param name The name of the NMS class to get
     *
     * @return The NMS Class
     */
    @JvmStatic
    fun getNMSClass(name: String): Class<*> {
        return Class.forName("net.minecraft.server.$nmsVersion.$name")
    }

    /**
     * Gets the player connection
     *
     * @param player The player to get the connection for
     * @return The player connection
     */
    @JvmStatic
    fun getPlayerConnection(player: Player): Any {
        val getHandle = player.javaClass.getMethod("getHandle")
        val nmsPlayer = getHandle.invoke(player)
        val conField = nmsPlayer.javaClass.getField("playerConnection")
        return conField.get(nmsPlayer)
    }

    /**
     * Sends a packet to a player
     *
     * @param player The player
     * @param packet The packet
     */
    fun sendPacket(p: Player, packet: Any) {
        val plrConnection = getPlayerConnection(p)
        plrConnection.javaClass.getMethod("sendPacket", getNMSClass("Packet")).invoke(plrConnection,
                packet)
    }
}