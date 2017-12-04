package me.mrkirby153.kcutils.command

import me.mrkirby153.kcutils.Chat
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerChatTabCompleteEvent
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * A command manager for commands
 */
class CommandManager private constructor(plugin: JavaPlugin) : Listener {

    /**
     * A list of registered commands
     */
    private val commands = HashMap<String, BaseCommand<out JavaPlugin>>()


    init {
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    @EventHandler
    fun commandPreProcess(event: PlayerCommandPreprocessEvent) {
        if (!event.message.startsWith("/"))
            return
        var commandName = event.message.substring(1)
        var args: Array<String>? = null
        if (commandName.contains(" ")) {
            commandName = commandName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            args = event.message.substring(event.message.indexOf(' ') + 1).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        if (args == null) {
            args = arrayOf()
        }
        val command = findCommand(commandName)
        if (command != null) {
            event.isCancelled = true
            if (command.permission != null && !event.player.hasPermission(command.permission)) {
                if (!event.player.isOp) {
                    event.player.spigot().sendMessage(Chat.error("You do not have permission to perform this command! You are missing the permission " + command.permission))
                    return
                }
            }
            command.setAlasUsed(commandName)
            command.execute(event.player, args)
        }
    }

    /**
     * Registers a command
     *
     * @param command The [Command][BaseCommand] to register
     */
    fun registerCommand(command: BaseCommand<out JavaPlugin>) {
        this.commands.put(command.name.toLowerCase(), command)
    }

    @EventHandler
    fun tabComplete(event: PlayerChatTabCompleteEvent) {
        var commandName = event.chatMessage
        var args: Array<String>? = null
        if (commandName.contains(" ")) {
            commandName = commandName.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()[0]
            args = event.chatMessage.substring(event.chatMessage.indexOf(' ') + 1).split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        }
        if (args == null)
            args = arrayOf()
        val command = findCommand(commandName)
        if (command != null) {
            val suggestions = command.tabComplete(event.player, commandName, args)
            event.tabCompletions.clear()
            event.tabCompletions.addAll(suggestions)
        }
    }

    /**
     * Finds a command by its name or alias
     *
     * @param command The command
     * @return The [Command][BaseCommand] or null if one wasn't found
     */
    private fun findCommand(command: String): BaseCommand<out JavaPlugin>? {
        if (this.commands.containsKey(command.toLowerCase()))
            return this.commands[command]
        // Find the command by its alias
        for (c in commands.values) {
            if (c.isAlias(command))
                return c
        }
        return null
    }

    companion object {

        private var instance: CommandManager? = null

        /**
         * Initializes the manager (Registers events)
         *
         * @param plugin The plugin to initialize for
         */
        fun initialize(plugin: JavaPlugin) {
            if (instance == null) {
                instance = CommandManager(plugin)
            }
        }

        /**
         * Gets the Command Manager instance
         *
         * @return The command manager
         */
        fun instance(): CommandManager? {
            return instance
        }
    }
}
