package me.mrkirby153.kcutils.flags

import me.mrkirby153.kcutils.command.CommandManager
import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Module
import net.md_5.bungee.api.ChatColor
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.World
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class FlagModule(plugin: JavaPlugin) : Module<JavaPlugin>("WorldFlags", plugin) {

    private val worldSettings = HashMap<String, FlagSettings>()

    operator fun get(world: World, flag: WorldFlags): Boolean {
        return worldSettings[world.name]?.isSet(flag) ?: flag.defaultValue()
    }

    fun getFlags(toActOn: World): FlagSettings {
        return worldSettings[toActOn.name] ?: FlagSettings(toActOn.name)
    }

    fun initialize(world: World): FlagSettings {
        var settings: FlagSettings? = this.worldSettings[world.name]
        if (settings == null)
            settings = FlagSettings(world.name)
        val finalSettings = settings
        Arrays.stream(WorldFlags.values()).forEach { flag -> finalSettings.setFlag(flag, flag.defaultValue()) }
        this.worldSettings.put(world.name, settings)
        return settings
    }

    fun initialized(world: World): Boolean {
        return this.worldSettings.containsKey(world.name)
    }

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

    fun shouldCancel(world: World, flag: WorldFlags): Boolean {
        return !get(world, flag)
    }

    override fun init() {
        registerListener(FlagListener(this))
        if (CommandManager.instance() != null)
            CommandManager.instance().registerCommand(FlagCommand(plugin, this))
        else
            log("[WARN] Command manager not initialized! " + FlagCommand::class.java + " needs to be registered manually!")
    }
}
