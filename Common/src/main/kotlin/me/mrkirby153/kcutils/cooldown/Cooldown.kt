package me.mrkirby153.kcutils.cooldown

class Cooldown<in T>(private val defaultDuration: Long) {

    private val map = mutableMapOf<T, CooldownEntry>()

    fun check(k: T): Boolean = (map[k]?.expiresOn ?: 0) < System.currentTimeMillis()

    @JvmOverloads
    fun use(k: T, duration: Long = defaultDuration) {
        map[k] = CooldownEntry(System.currentTimeMillis() + duration, duration)
    }

    fun getTimeLeft(k: T): Long = (map[k]?.expiresOn ?: System.currentTimeMillis()) - System.currentTimeMillis()

    fun getPercentComplete(k: T): Double {
        val entry = map[k] ?: return 1.0
        val elapsedTime = entry.expiresOn - System.currentTimeMillis()
        return elapsedTime / entry.duration.toDouble()
    }
}

data class CooldownEntry(val expiresOn: Long, val duration: Long)