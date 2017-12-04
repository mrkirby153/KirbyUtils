package me.mrkirby153.kcutils.cooldown

/**
 * A class representing a cooldown
 *
 * @param T                     The type of cooldown
 * @property defaultDuration    The default cooldown duration
 * @property name               The name of the cooldown
 * @property notify             If the user should be notified when the cooldown expires
 */
class Cooldown<T>(private val defaultDuration: Long, val name: String, var notify: Boolean = false) {

    constructor(defaultDuration: Long, name: String) : this(defaultDuration, name, false)

    private val map = mutableMapOf<T, CooldownEntry>()

    private val pendingNotify = mutableSetOf<T>()

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
        if(notify)
            pendingNotify.add(k)
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

    /**
     * Gets the elements pending notification on coodown complete
     *
     * @return A HashSet of objects pending notification
     */
    fun getPendingNotifcations() = this.pendingNotify.toHashSet()

    /**
     * Removes the pending notification for an object
     *
     * @param k The object to remove
     */
    fun removeNotifcation(k: T){
        this.pendingNotify.remove(k)
    }
}

/**
 * Keeps track of the expiry and the duration of the cooldown
 *
 * @property expiresOn  The epoch time (in ms) that the cooldown expires at
 * @property duration   The duration of the cooldown (in ms)
 */
data class CooldownEntry(val expiresOn: Long, val duration: Long)