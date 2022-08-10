package me.mrkirby153.kcutils.reflections

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.lang.reflect.Constructor
import java.lang.reflect.Field
import java.lang.reflect.Method


object Reflections {

    /**
     * A mapping of classes to their primitive types
     */
    private val primitiveTypeMap = mutableMapOf<Class<*>, Class<*>>()

    init {
        primitiveTypeMap[java.lang.Boolean::class.java] = java.lang.Boolean.TYPE
        primitiveTypeMap[java.lang.Character::class.java] = Character.TYPE
        primitiveTypeMap[java.lang.Byte::class.java] = java.lang.Byte.TYPE
        primitiveTypeMap[java.lang.Short::class.java] = java.lang.Short.TYPE
        primitiveTypeMap[java.lang.Integer::class.java] = Integer.TYPE
        primitiveTypeMap[java.lang.Long::class.java] = java.lang.Long.TYPE
        primitiveTypeMap[java.lang.Double::class.java] = java.lang.Double.TYPE
        primitiveTypeMap[java.lang.Void::class.java] = Void.TYPE
        primitiveTypeMap[java.lang.Float::class.java] = java.lang.Float.TYPE
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
     *
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
     *
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
     *
     * @return The class instance
     */
    @JvmStatic
    fun getInstance(clazz: Class<*>, vararg params: Any): Any {
        val types = params.map { mapToPrimitive(it.javaClass) }.toTypedArray()

        // Find the constructor

        var constructor: Constructor<*>? = null
        clazz.constructors.filter { it.parameterCount == types.size }.forEach { potentialConstructor ->
            val constructorParamTypes = potentialConstructor.parameterTypes
            var index = 0

            while (index < constructorParamTypes.size) {
                // Try the base class then try the super classes
                var found = false
                var c: Class<*>? = types[index]
                while (c != null) {
                    if (constructorParamTypes[index] == c) {
                        found = true
                        break
                    } else {
                        c = c.superclass
                    }
                }
                if (!found) {
                    // This constructor param does not match. No sense in continuing further
                    return@forEach
                } else {
                    // Successfully found the class, lets keep going
                    index++
                }
            }
            // Presumably at this point we've matched all the parameters
            constructor = potentialConstructor
        }
        if (constructor == null)
            throw NoSuchMethodException("${clazz.canonicalName}.<init>(${types.joinToString(
                    ",") { it.canonicalName }})")

        return constructor!!.newInstance(*params)
    }

    /**
     * Gets an instance of a NMS class with the given constructor parameters
     *
     * @param nmsClass The NMS Class name to instantiate
     * @param params The constructor parameters
     *
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
     *
     * @return The primitive type of the class, if applicable, or the class passed in
     */
    @JvmStatic
    fun mapToPrimitive(clazz: Class<*>): Class<*> {
        return this.primitiveTypeMap.getOrDefault(clazz, clazz)
    }

    /**
     * Invokes a method on a class
     *
     * @param clazz The class to execute the method on
     * @param methodName The method name to execute
     * @param instance An instance of the class or null if invoking a static method
     * @param params Any parameters
     *
     * @return The method's result
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> invokeMethod(clazz: Class<*>, methodName: String, instance: Any?, vararg params: Any): T {
        val paramTypes = params.map { mapToPrimitive(it.javaClass) }.toTypedArray()
        val method: Method = getMethod(clazz, methodName, paramTypes)
        method.isAccessible = true
        return method.invoke(instance, *params) as T
    }


    /**
     * Invokes a method on a class
     *
     * @param instance The instance to invoke the method on
     * @param method The method to invoke
     * @param params Any method parameters
     */
    @JvmStatic
    fun <T> invoke(instance: Any, method: String,
                   vararg params: Any) = invokeMethod<T>(instance.javaClass,
            method, instance, *params)

    /**
     * Sets a field on a class
     *
     * @param clazz The class to set the field on
     * @param fieldName The field name
     * @param instance An instance of the class
     * @param value The value to set
     */
    @JvmStatic
    fun setField(clazz: Class<*>, fieldName: String, instance: Any?, value: Any?) {
        val field: Field = getField(clazz, fieldName)
        field.isAccessible = true
        field.set(instance, value)
    }

    /**
     * Sets a field on an object
     *
     * @param instance The instance to use
     * @param field The field name to use
     * @param value The value to set
     */
    @JvmStatic
    fun set(instance: Any, field: String, value: Any?) = setField(instance.javaClass, field,
            instance, value)

    /**
     * Gets a field's value
     *
     * @param clazz The class
     * @param fieldName The name of the field
     * @param instance The instance of the object
     *
     * @return The field's value
     */
    @Suppress("UNCHECKED_CAST")
    @JvmStatic
    fun <T> get(clazz: Class<*>, fieldName: String, instance: Any): T {
        val field = getField(clazz, fieldName)
        return field.get(instance) as T
    }

    /**
     * Gets a field's value
     *
     * @param obj The object
     * @param name The name of the field
     * @return The field
     */
    @JvmStatic
    fun <T> get(obj: Any, name: String) = get<T>(obj.javaClass, name, obj)


    /**
     * Gets an enum's value
     *
     * @param clazz The enum class
     * @param value The enum name
     * @return The enum
     */
    @JvmStatic
    fun getEnumValue(clazz: Class<*>, value: String): Any {
        return invokeMethod(clazz, "valueOf", null, value)
    }


    /**
     * Gets a field from a class
     *
     * @param clazz The class
     * @param fieldName The name
     *
     * @return The field
     * @throws NoSuchFieldException If the field isn't found
     */
    private fun getField(clazz: Class<*>,
                         fieldName: String): Field {
        var c: Class<*>? = clazz
        var field: Field? = null
        while (c != null) {
            field = c.declaredFields.firstOrNull { it.name == fieldName }
            if (field != null)
                break
            c = c.superclass // Recurse up
        }
        if (field == null)
            throw NoSuchFieldException(fieldName)
        field.isAccessible = true
        return field
    }


    /**
     * Gets a method from a class, recursing up the inheritance tree
     *
     * @param clazz The class
     * @param methodName The name of the method
     * @param methodParams The method param types
     *
     * @return The method
     * @throws NoSuchElementException If the method isn't found
     */
    private fun getMethod(clazz: Class<*>, methodName: String,
                          methodParams: Array<Class<*>>): Method {
        var c: Class<*>? = clazz
        val methods = mutableListOf<Method>()

        while (c != null) {
            methods.addAll(
                    c.methods.filter { it.name == methodName && it.parameterCount == methodParams.size })
            c = c.superclass
        }

        var method: Method? = null
        methods.forEach { potentialMethod ->
            val params = potentialMethod.parameterTypes
            var index = 0

            while (index < params.size) {
                var found = false
                var c1: Class<*>? = methodParams[index]
                while (c1 != null) {
                    if (params[index] == c1) {
                        found = true
                        break
                    } else {
                        c1 = c1.superclass
                    }
                }
                if (!found) {
                    return@forEach
                } else {
                    index++
                }
            }
            method = potentialMethod
        }
        // Find the method that matches the parameters
        if (method == null) {
            throw NoSuchMethodException(
                    "${clazz.canonicalName}.$methodName(${methodParams.joinToString(
                            ",") { it.canonicalName }})")
        }
        return method!!
    }

}