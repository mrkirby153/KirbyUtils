package me.mrkirby153.kcutils.timing

import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Throttles execution of an event. Subsequent calls will be dropped until the quiet period has passed.
 */
class Throttler<T>(private val event: Consumer<T?>?) {

    private val executed = mutableMapOf<T?, Long>()

    /**
     * Triggers throttled execution of the event
     *
     * @param key The key to pass into the event
     * @param time The throttle time
     * @param unit The time units
     */
    fun trigger(key: T?, time: Long, unit: TimeUnit) {
        val next = executed[key]
        if (next != null) {
            if (System.currentTimeMillis() < next) {
                return // Discard the event because it's throttled
            }
        }
        val runNext = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(time, unit)
        executed[key] = runNext
        event?.accept(key)
    }

    /**
     * Checks if the function is throttled for the given key
     *
     * @param key The key
     *
     * @return True if the key's execution is throttled
     */
    fun throttled(key: T): Boolean {
        val next = executed[key]
        if (next != null) {
            return System.currentTimeMillis() < next
        }
        return false
    }

    /**
     * Returns the number of milliseconds left in the provided key's quiet period
     *
     * @param key The key
     *
     * @return The amount of time (in milliseconds) left in the key's quiet period
     */
    fun getTimeRemaining(key: T): Long {
        val time = executed[key] ?: 0
        return time - System.currentTimeMillis()
    }
}