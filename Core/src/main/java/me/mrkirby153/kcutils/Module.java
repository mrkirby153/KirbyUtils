package me.mrkirby153.kcutils;

import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Abstract framework for utilities
 */
public abstract class Module<T extends JavaPlugin> {

    private T plugin;
    private String name;
    private String version;
    private boolean loaded = false;

    public Module(String name, String version, T plugin) {
        this.name = name;
        this.plugin = plugin;
        this.version = version;
    }

    /**
     * Gets the name of the module
     *
     * @return The module's name
     */
    public String getName() {
        return name;
    }

    /**
     * Gets the version of the module
     *
     * @return The module's version
     */
    public String getVersion() {
        return version;
    }

    /**
     * Loads the module
     */
    public void load() {
        if(loaded){
            throw new IllegalArgumentException("Attempting to reload an already loaded module!");
        }
        long startTime = System.currentTimeMillis();
        log("Loading version " + version);
        init();
        long endTime = System.currentTimeMillis();
        long elapsedTime = endTime - startTime;
        log("Loaded in " + (elapsedTime < 1 ? "< 1 millisecond" : Time.format(1, elapsedTime, Time.TimeUnit.FIT)));
        loaded = true;
    }

    /**
     * Gets the plugin that loaded the module
     *
     * @return The plugin
     */
    protected T getPlugin() {
        return plugin;
    }

    protected abstract void init();

    /**
     * Log a message to the plugin's log
     *
     * @param message The message
     */
    protected void log(String message) {
        plugin.getLogger().info(String.format("[%s] %s", name.toUpperCase(), message));
    }

    /**
     * Register a listener
     *
     * @param listener A listener to register
     */
    protected void registerListener(Listener listener) {
        plugin.getServer().getPluginManager().registerEvents(listener, plugin);
    }
}