package me.mrkirby153.kcutils.cooldown

/**
 * A class representing a cooldown
 *
 * @param T                     The type of cooldown
 * @property defaultDuration    The default cooldown duration
 */
class Cooldown<in T>(private val defaultDuration: Long) {

    private val map = mutableMapOf<T, CooldownEntry>()

    /**
     * Checks if the cooldown has expired
     */
    fun check(k: T): Boolean = (map[k]?.expiresOn ?: 0) < System.currentTimeMillis()

    /**
     * Updates the cooldown
     *
     * @param k         The object using the cooldown
     * @param duration  The duration of the cooldown
     */
    @JvmOverloads
    fun use(k: T, duration: Long = defaultDuration) {
        map[k] = CooldownEntry(System.currentTimeMillis() + duration, duration)
    }

    /**
     * Gets the time left in the cooldown
     *
     * @param k The object using the cooldown
     *
     * @return The time (in ms) remaining on the cooldown
     */
    fun getTimeLeft(k: T): Long = (map[k]?.expiresOn ?: System.currentTimeMillis()) - System.currentTimeMillis()

    /**
     * Gets the percent complete of the cooldown
     *
     * @param k The object using the cooldown
     *
     * @return The percentage complete
     */
    fun getPercentComplete(k: T): Double {
        val entry = map[k] ?: return 1.0
        val elapsedTime = entry.expiresOn - System.currentTimeMillis()
        return elapsedTime / entry.duration.toDouble()
    }
}

/**
 * Keeps track of the expiry and the duration of the cooldown
 *
 * @property expiresOn  The epoch time (in ms) that the cooldown expires at
 * @property duration   The duration of the cooldown (in ms)
 */
data class CooldownEntry(val expiresOn: Long, val duration: Long)