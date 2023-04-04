package me.mrkirby153.kcutils.utils

import java.util.Arrays
import java.util.function.Predicate
import java.util.stream.Collectors
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or


/**
 * A wrapper class for bitfields that simplifies interactions with them.
 *
 * To turn an enum into a binary flag, use [binaryFlag]  or [BinaryFlag.create].
 *
 * ## Kotlin Example
 * ```kotlin
 * val flag = binaryFlag<MyEnum>()
 * ```
 * ## Java Example
 * ```java
 * BinaryFlag<MyEnum> = BinaryFlag.create(MyEnum.class)
 * ```
 */
class BinaryFlag<T : Enum<T>> constructor(
    private val type: Class<T>,
    private val value: Long
) : Iterable<T> {
    private val constructor = javaClass.getConstructor(Class::class.java, Long::class.java)

    private fun type() = type

    private fun create(value: Long): BinaryFlag<T> {
        return constructor.newInstance(type, value)
    }

    /**
     * Returns true if this [BinaryFlag] is empty
     */
    fun isEmpty() = value == 0L

    /**
     * Returns true if this [BinaryFlag] has the given [value]
     */
    fun has(value: T) = this.value and makeMask(value) != 0L

    /**
     * Returns true if this [BinaryFlag] has any of the given [values]
     */
    fun hasAny(vararg values: T) = this.value and makeMask(*values) != 0L

    /**
     * Returns true if this [BinaryFlag] has any of the given [values]
     */
    fun hasAny(values: BinaryFlag<T>) = this.value and values.value != 0L

    /**
     * Returns true if this [BinaryFlag] has all of the given [values]
     */
    fun hasAll(vararg values: T): Boolean {
        val mask = makeMask(*values)
        return value and mask == mask
    }

    /**
     * Returns true if this [BinaryFlag] has all of the given [values]
     */
    fun hasAll(values: BinaryFlag<T>): Boolean {
        val mask = values.value
        return value and mask == mask
    }

    /**
     * Creates a new [BinaryFlag] that includes the given [v]
     */
    fun and(v: T) = create(value or makeMask(v))

    /**
     * Creates a new [BinaryFlag] that includes the given [vs]
     */
    fun and(vararg vs: T) = create(value or makeMask(*vs))

    /**
     * Creates a new [BinaryFlag] tthat includes the given [flags]
     */
    fun and(flags: BinaryFlag<T>) = create(value or flags.value)

    /**
     * Creates a new [BinaryFlag] that consists of all values except [v]
     */
    fun except(v: T) = create((value and makeMask(v).inv()))

    /**
     * Creates a new [BinaryFlag] that consists of all values except [vs]
     */
    fun except(vararg vs: T) = create(value and makeMask(*vs).inv())

    /**
     * Creates a new [BinaryFlag] that consists of all values except [flags]
     */
    fun except(flags: BinaryFlag<T>) = create(value and flags.value.inv())

    /**
     * Creates a new [BinaryFlag] whose value holds all flags that are stored currently and [v]
     */
    fun onlyIn(v: T) = create(value and makeMask(v))

    /**
     * Creates a new [BinaryFlag] whose value holds all flags that are stored currently and [vs]
     */
    fun onlyIn(vararg vs: T) = create(value and makeMask(*vs))

    /**
     * Creates a new [BinaryFlag] whose value holds all flags that are stored currently and [flags]
     */
    fun onlyIn(flags: BinaryFlag<T>) = create(value and flags.value)

    /**
     * Create a new [BinaryFlag] whose value consists of all flags that match [predicate]
     */
    fun where(predicate: Predicate<T>) = stream().filter { predicate.test(it) }.toList().run {
        create(value and makeMask(this))
    }

    /**
     * Serializes the [BinaryFlag] into a [Byte]
     */
    fun serialize() = value

    /**
     * Returns a stream of all flags
     */
    fun stream() = Arrays.stream(type().enumConstants).filter { this.has(it) }

    /**
     * Returns an iterator for all flags currently set
     */
    override fun iterator(): Iterator<T> = stream().iterator()

    override fun toString(): String {
        return "[" + stream().map { it.name }.collect(Collectors.joining(", ")) + "]"
    }

    companion object {
        /**
         * Creates a mask of the given [value]
         */
        @JvmStatic
        fun <T : Enum<T>> makeMask(value: Enum<T>) = (1 shl value.ordinal).toLong()

        /**
         * Creates a mask of the given [values]
         */
        @JvmStatic
        fun <T : Enum<T>> makeMask(values: Iterable<Enum<T>>): Long {
            var value = 0L
            values.forEach {
                value = value or makeMask(it)
            }
            return value
        }

        @JvmStatic
        fun <T : Enum<T>> makeMask(vararg values: Enum<T>) = makeMask(values.asList())

        /**
         * Creates a new [BinaryFlag] of the given [type] and the provided [value]
         */
        @JvmStatic
        @JvmOverloads
        fun <T : Enum<T>> create(type: Class<T>, value: Long = 0) = BinaryFlag(type, value)

        /**
         * Creates a new [BinaryFlag] of the given [type] and provided [values]
         */
        @JvmStatic
        fun <T : Enum<T>> create(type: Class<T>, vararg values: T) =
            BinaryFlag(type, BinaryFlag.makeMask(*values))
    }
}

inline fun <reified T : Enum<T>> binaryFlag(value: Long = 0): BinaryFlag<T> =
    BinaryFlag(T::class.java, value)

inline fun <reified T : Enum<T>> binaryFlag(vararg values: T) =
    BinaryFlag(T::class.java, BinaryFlag.makeMask(*values))