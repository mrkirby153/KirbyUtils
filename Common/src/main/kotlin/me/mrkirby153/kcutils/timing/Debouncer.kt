package me.mrkirby153.kcutils.timing

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.Executors
import java.util.concurrent.Future
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.ThreadFactory
import java.util.concurrent.TimeUnit
import java.util.function.Consumer

/**
 * Debounces (delays execution) a [Consumer].
 *
 * The debouncer has three modes:
 * * **LEADING** - The first event will call the [Consumer] and any subsequent triggers will be ignored
 * until the time period has passed
 * * **TRAILING** - The [Consumer] is called after the provided delay. Any triggers during the wait period
 * will replace the event and reset the timer
 * * **BOTH** - The first event will call the [Consumer] and any subsequent triggers will be queued until
 * the period has elapsed
 */
class Debouncer<T>(private val runnable: Consumer<T?>, threadFactory: ThreadFactory? = null,
                   private val mode: Mode = Mode.TRAILING) {

    private val scheduler: ScheduledExecutorService
    private val delayedMap = ConcurrentHashMap<T?, Future<*>>()

    private var nextRunMap = ConcurrentHashMap<T?, Long>()

    init {
        if (threadFactory != null) {
            scheduler = Executors.newSingleThreadScheduledExecutor(threadFactory)
        } else {
            scheduler = Executors.newSingleThreadScheduledExecutor()
        }
    }

    /**
     * Triggers the debounce with the given delay
     *
     * @param key The key
     * @param delay The delay
     * @param unit The units for the delay
     */
    fun debounce(key: T?, delay: Long, unit: TimeUnit) {
        if (mode == Mode.TRAILING) {
            val prev = delayedMap.put(key, scheduler.schedule({
                try {
                    runnable.accept(key)
                } finally {
                    delayedMap.remove(key)
                }
            }, delay, unit))
            prev?.cancel(true)
        }
        if (mode == Mode.LEADING) {
            if (System.currentTimeMillis() > nextRunMap[key] ?: 0) {
                try {
                    runnable.accept(key)
                } finally {
                    nextRunMap[key] = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(
                            delay,
                            unit)
                }
            }
        }
        if (mode == Mode.BOTH) {
            if (System.currentTimeMillis() > nextRunMap[key] ?: 0) {
                try {
                    runnable.accept(key)
                } finally {
                    nextRunMap[key] = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(
                            delay,
                            unit)
                }
            } else {
                val prev = delayedMap.put(key, scheduler.schedule({
                    try {
                        runnable.accept(key)
                    } finally {
                        delayedMap.remove(key)
                    }
                }, (nextRunMap[key] ?: System.currentTimeMillis()) - System.currentTimeMillis(),
                        TimeUnit.MILLISECONDS))
                prev?.cancel(true)
            }
        }
    }

    /**
     * Shuts down the debouncer
     */
    fun shutdown() {
        scheduler.shutdownNow()
    }

    /**
     * If the debouncer has any tasks currently pending execution
     * **Note:** This only applies when the mode is `TRAILING` or `BOTH`
     *
     * @return True if there are events queued up that are pending execution.
     */
    fun hasPending(): Boolean {
        if (mode == Mode.TRAILING || mode == Mode.BOTH) {
            return delayedMap.size > 0
        }
        return false
    }

    /**
     * The mode of the [Debouncer]
     */
    enum class Mode {
        LEADING,
        TRAILING,
        BOTH
    }
}