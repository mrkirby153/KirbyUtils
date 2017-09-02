package me.mrkirby153.kcutils.event

import me.mrkirby153.kcutils.Module
import org.bukkit.plugin.java.JavaPlugin

class UpdateEventHandler(plugin: JavaPlugin) : Module<JavaPlugin>("Update Event", plugin), Runnable {

    private var ticks = 0

    override fun run() {
        // Prevent some sort of overflow if it's running too long. That could be bad
        if (++ticks >= Integer.MAX_VALUE) {
            ticks = 1
        }
        UpdateType.values()
                .filter { it.elapsed(ticks) }
                .forEach { plugin.server.pluginManager.callEvent(UpdateEvent(it)) }
    }

    override fun init() {
        scheduleRepeating(0L, 1L, this)
    }
}
