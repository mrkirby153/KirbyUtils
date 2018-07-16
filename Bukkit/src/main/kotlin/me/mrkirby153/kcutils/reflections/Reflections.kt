package me.mrkirby153.kcutils.reflections

import org.bukkit.Bukkit
import org.bukkit.entity.Player


object Reflections {

    /**
     * A mapping of classes to their primitive types
     */
    private val primitiveTypeMap = mutableMapOf<Class<*>, Class<*>>()

    init {
        primitiveTypeMap[java.lang.Boolean::class.java] = java.lang.Boolean.TYPE
        primitiveTypeMap[java.lang.Character::class.java] = java.lang.Character.TYPE
        primitiveTypeMap[java.lang.Byte::class.java] = java.lang.Byte.TYPE
        primitiveTypeMap[java.lang.Short::class.java] = java.lang.Short.TYPE
        primitiveTypeMap[java.lang.Integer::class.java] = java.lang.Integer.TYPE
        primitiveTypeMap[java.lang.Long::class.java] = java.lang.Long.TYPE
        primitiveTypeMap[java.lang.Double::class.java] = java.lang.Double.TYPE
        primitiveTypeMap[java.lang.Void::class.java] = java.lang.Void.TYPE
    }

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
     * Gets a NMS Class wrapped in [WrappedReflectedClass] for easy reflection access
     *
     * @param name The name of the NMS class
     * @return A [WrappedReflectedClass] of the provided NMS class
     */
    @JvmStatic
    fun getWrappedNMSClass(name: String): WrappedReflectedClass {
        return WrappedReflectedClass(getNMSClass(name))
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
     * Gets an instance of a class with the given parameters
     *
     * @param clazz The class to instantiate
     * @param params The constructor parameters
     * @return The class instance
     */
    @JvmStatic
    fun getInstance(clazz: Class<*>, vararg params: Any): Any {
        val types = params.map { mapToPrimitive(it.javaClass) }.toTypedArray()
        val constructor = clazz.getConstructor(*types)
        return constructor.newInstance(*params)
    }

    /**
     * Gets an instance of a NMS class with the given constructor parameters
     *
     * @param nmsClass The NMS Class name to instantiate
     * @param params The constructor parameters
     * @return The class instance
     */
    @JvmStatic
    fun getInstance(nmsClass: String, vararg params: Any): Any {
        return getInstance(getNMSClass(nmsClass), *params)
    }

    /**
     * Sends a packet to a player
     *
     * @param p The player
     * @param packet The packet
     */
    @JvmStatic
    fun sendPacket(p: Player, packet: Any) {
        val plrConnection = getPlayerConnection(p)
        plrConnection.javaClass.getMethod("sendPacket", getNMSClass("Packet")).invoke(plrConnection,
                packet)
    }

    /**
     * Sends a packet to a player
     *
     * @param p The player
     * @param packet The [WrappedReflectedClass] representing a packet
     */
    @JvmStatic
    fun sendPacket(p: Player, packet: WrappedReflectedClass) {
        val instance = packet.get()
        if (!getNMSClass("Packet").isAssignableFrom(instance.javaClass))
            throw IllegalArgumentException(
                    "Attempting to send non-packet (${instance.javaClass.canonicalName}) to a player")
        sendPacket(p, packet.get())
    }

    /**
     * Converts a class to its primitive type, if applicable
     *
     * @param clazz The class to map to its primitive type
     * @return The primitive type of the class, if applicable, or the class passed in
     */
    fun mapToPrimitive(clazz: Class<*>): Class<*> {
        return this.primitiveTypeMap.getOrDefault(clazz, clazz)
    }
}