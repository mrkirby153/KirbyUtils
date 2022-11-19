package me.mrkirby153.kcutils.utils

import java.util.Arrays
import java.util.function.Predicate
import java.util.stream.Collectors
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or

abstract class BinaryFlag<T : Enum<T>, F : BinaryFlag<T, F>> protected constructor(
    private val value: Byte
) : Iterable<T> {
    abstract fun type(): Class<T>
    abstract fun create(value: Byte): F

    fun isEmpty() = value == 0.toByte()

    fun has(value: T) = this.value and makeMask(value) != 0.toByte()

    fun hasAny(vararg values: T) = this.value and makeMask(*values) != 0.toByte()
    fun hasAny(values: F) = this.value and values.value != 0.toByte()

    fun hasAll(vararg values: T): Boolean {
        val mask = makeMask(*values)
        return value and mask == mask
    }

    fun hasAll(values: F): Boolean {
        val mask = values.value
        return value and mask == mask
    }

    fun and(v: T) = create(value or makeMask(v))
    fun and(vararg vs: T) = create(value or makeMask(*vs))
    fun and(flags: F) = create(value or flags.value)

    fun except(v: T) = create((value and makeMask(v).inv()))
    fun except(vararg vs: T) = create(value and makeMask(*vs).inv())
    fun except(flags: F) = create(value and flags.value.inv())

    fun onlyIn(v: T) = create(value and makeMask(v))
    fun onlyIn(vararg vs: T) = create(value and makeMask(*vs))
    fun onlyIn(flags: F) = create(value and flags.value)

    fun where(predicate: Predicate<T>) = stream().filter { predicate.test(it) }.toList().run {
        create(value and makeMask(this))
    }

    fun serialize() = value

    fun stream() = Arrays.stream(type().enumConstants).filter { this.has(it) }

    override fun iterator(): Iterator<T> = stream().iterator()

    override fun toString(): String {
        return "[" + stream().map { it.name }.collect(Collectors.joining(", ")) + "]"
    }

    companion object {
        @JvmStatic
        fun <T : Enum<T>> makeMask(value: Enum<T>) = (1 shl value.ordinal).toByte()

        @JvmStatic
        fun <T : Enum<T>> makeMask(values: Iterable<Enum<T>>): Byte {
            var value = 0.toByte()
            values.forEach {
                value = value or makeMask(it)
            }
            return value
        }

        @JvmStatic
        fun <T : Enum<T>> makeMask(vararg values: Enum<T>) = makeMask(values.asList())
    }
}

