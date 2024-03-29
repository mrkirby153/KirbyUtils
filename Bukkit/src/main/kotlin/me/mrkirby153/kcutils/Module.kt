package me.mrkirby153.kcutils

import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import kotlin.system.measureTimeMillis

/**
 * A class for creating "sub-plugins" in one main plugin
 *
 * Modules are used to logically separate parts of plugins from one another.
 *
 * @param name      The name of the plugin
 * @param plugin    The owning [JavaPlugin]
 */
abstract class Module<out T : JavaPlugin>(private val name: String, protected val plugin: T) {

    /**
     * If the module has been loaded
     */
    var loaded: Boolean = false

    /**
     * The configuration file of the main plugin
     */
    val config: FileConfiguration
        get() = plugin.config

    /**
     * Loads the module
     */
    fun load() {
        if (loaded)
            throw IllegalStateException("Attempting to load an already loaded module!")
        val loadTime = measureTimeMillis {
            try {
                init()
            } catch (e: Exception) {
                log("[ERROR] Caught exception while loading. (${e.message})")
                e.printStackTrace()
                return
            }
        }
        loaded = true
        log("Loaded in ${if (loadTime < 1) "< 1 millisecond" else Time.format(1, loadTime, Time.TimeUnit.FIT)}")
    }

    /**
     * Unloads the module
     */
    fun unload() {
        if (!loaded)
            throw IllegalStateException("Attempting to unload an already unloaded module!")
        val unloadTime = measureTimeMillis {
            try {
                disable()
            } catch (e: Exception) {
                log("[ERROR] Caught exception while unloading. (${e.message})")
                e.printStackTrace()
                return
            }
        }
        loaded = false
        log("Loaded in ${if (unloadTime < 1) "< 1 millisecond" else Time.format(1, unloadTime, Time.TimeUnit.FIT)}")
    }

    /**
     * Called when the module is loaded
     */
    open fun init() {

    }

    /**
     * Called when the module is unloaded
     */
    open fun disable() {

    }


    /**
     * Logs a message
     *
     * @param msg The message to log
     */
    fun log(msg: Any) {
        plugin.logger.info(String.format("[%s] %s", name.uppercase(), msg))
    }

    /**
     * Registers a listener
     *
     * @param listener The listener to register
     */
    fun registerListener(listener: Listener) {
        plugin.server.pluginManager.registerEvents(listener, plugin)
    }

    /**
     * Runs a task async
     *
     * @param task The task to run async
     */
    fun runAsync(task: () -> Unit) {
        plugin.server.scheduler.runTaskAsynchronously(plugin, task)
    }

    /**
     * Runs a task async
     *
     * @param runnable The task to run async
     */
    fun runAsync(runnable: Runnable) {
        plugin.server.scheduler.runTaskAsynchronously(plugin, runnable)
    }

    /**
     * Runs a task synchronously
     *
     * @param task The task to run sync
     */
    fun runSync(task: () -> Unit) {
        plugin.server.scheduler.runTask(plugin, task)
    }

    /**
     * Runs a task synchronously
     *
     * @param runnable The task to run sync
     */
    fun runSync(runnable: Runnable) {
        plugin.server.scheduler.runTask(plugin, runnable)
    }


    /**
     * Schedules a task to be run periodically
     *
     * @param delay The initial delay before running the task
     * @param interval The interval in which the task is to run
     * @param task The task to run
     */
    fun scheduleRepeating(delay: Long, interval: Long, task: () -> Unit) {
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, task, delay, interval)
    }

    /**
     * Schedules a task to be run periodically
     *
     * @param delay The initial delay before running the task
     * @param interval The interval in which the task is to run
     * @param runnable The task to run
     */
    fun scheduleRepeating(delay: Long, interval: Long, runnable: Runnable) {
        plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, runnable, delay, interval)
    }
}