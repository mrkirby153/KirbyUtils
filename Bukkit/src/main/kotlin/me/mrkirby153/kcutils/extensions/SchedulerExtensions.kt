package me.mrkirby153.kcutils.extensions

import org.bukkit.scheduler.BukkitRunnable

/**
 * Creates a [BukkitRunnable]
 */
inline fun runnable(
    crossinline body: BukkitRunnable.() -> Unit
) = object : BukkitRunnable() {
    override fun run() {
        this.body()
    }
}