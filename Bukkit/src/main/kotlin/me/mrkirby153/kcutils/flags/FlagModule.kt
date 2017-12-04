package me.mrkirby153.kcutils.flags

import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Module
import me.mrkirby153.kcutils.command.CommandManager
import net.md_5.bungee.api.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * A module for handling various flags on the world
 *
 * @param plugin    The owning plugin
 */
class FlagModule(plugin: JavaPlugin) : Module<JavaPlugin>("WorldFlags", plugin) {

    private val worldSettings = HashMap<String, FlagSettings>()

    /**
     * Gets a flag's status for the world
     *
     * @param world The world
     * @param flag  The flag
     *
     * @return True if the flag is set
     */
    operator fun get(world: World, flag: WorldFlags): Boolean {
        return worldSettings[world.name]?.isSet(flag) ?: flag.defaultValue()
    }

    /**
     * Gets all set flags on the world
     *
     * @param toActOn The world
     *
     * @return The flag settings
     */
    fun getFlags(toActOn: World): FlagSettings {
        return worldSettings[toActOn.name] ?: FlagSettings(toActOn.name)
    }

    /**
     * Initialize the world by setting all flags to their default values
     *
     * @param world The world to initialize
     *
     * @return The world's flag settings
     */
    fun initialize(world: World): FlagSettings {
        var settings: FlagSettings? = this.worldSettings[world.name]
        if (settings == null)
            settings = FlagSettings(world.name)
        val finalSettings = settings
        Arrays.stream(WorldFlags.values()).forEach { flag -> finalSettings.setFlag(flag, flag.defaultValue()) }
        this.worldSettings.put(world.name, settings)
        return settings
    }

    /**
     * Checks if the world has been initialized
     *
     * @param world The World to check
     *
     * @return True if the world has had its flags initialized
     */
    fun initialized(world: World): Boolean {
        return this.worldSettings.containsKey(world.name)
    }

    /**
     * Sets a flag in the world
     *
     * @param world     The world to set
     * @param flag      The flag to set
     * @param state     The state of the flag (enabled/disabled)
     * @param announce  If the flag change is to be announced
     */
    @JvmOverloads
    fun set(world: World, flag: WorldFlags, state: Boolean, announce: Boolean = true) {
        if (!worldSettings.containsKey(world.name))
            return
        worldSettings[world.name]?.setFlag(flag, state)
        if (announce)
            world.players.forEach { player ->
                val component = Chat.formattedChat(flag.friendlyName + "> ", Chat.TAG_COLOR, Chat.Style.BOLD)

                val s = Chat.formattedChat(if (state) "true" else "false", if (state) ChatColor.GREEN else ChatColor.RED, Chat.Style.BOLD)
                component.addExtra(s)
                player.spigot().sendMessage(component)
                player.playSound(player.location, Sound.BLOCK_NOTE_PLING, SoundCategory.MASTER, 1f, 1f)
            }
    }

    /**
     * Checks if the flag should be canceled
     *
     * @param world The world to check
     * @param flag  The flag to check
     *
     * @return True if the event should be canceled
     */
    fun shouldCancel(world: World, flag: WorldFlags): Boolean {
        return !get(world, flag)
    }

    override fun init() {
        registerListener(FlagListener(this))
        if (CommandManager.instance() != null)
            CommandManager.instance()?.registerCommand(FlagCommand(plugin, this))
        else
            log("[WARN] Command manager not initialized! " + FlagCommand::class.java + " needs to be registered manually!")
    }
}
