package me.mrkirby153.kcutils.flags

import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.command.BaseCommand
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * A command for setting world flags
 */
class FlagCommand(instance: JavaPlugin, private val module: FlagModule) : BaseCommand<JavaPlugin>(instance, "ku-flags", "kirbyutils.command.flag", arrayOf()) {

    override fun execute(player: Player, args: Array<String>) {
        val toActOn = player.world

        if (!module.initialized(toActOn)) {
            player.sendMessage(Chat.legacyError("World is not initialized for flags!"))
            return
        }

        if (args.isEmpty()) {
            // Build a list of flags and their state
            val flags = StringBuilder()
            flags.append(ChatColor.BLUE).append(toActOn.name).append(" Flags> ")

            val settings = module.getFlags(toActOn)
            settings.setFlags.forEach { flag, state ->
                flags.append(if (state) ChatColor.GREEN else ChatColor.RED)
                flags.append(flag.toString()).append(", ")
            }
            player.sendMessage(flags.toString().substring(0, flags.length - 2))
        }
        if (args.size == 2) {
            val flag = args[0]
            var all = false
            if (flag.equals("all", ignoreCase = true)) {
                all = true
            }
            val state = java.lang.Boolean.parseBoolean(args[1])
            if (!all) {
                val f: WorldFlags
                try {
                    f = WorldFlags.valueOf(flag.toUpperCase())
                } catch (e: IllegalArgumentException) {
                    player.spigot().sendMessage(Chat.error("That is not a valid flag!"))
                    return
                }

                module.set(toActOn, f, state)
            } else {
                Arrays.stream(WorldFlags.values()).forEach { f -> module.set(toActOn, f, state) }
            }
        }
    }
}
