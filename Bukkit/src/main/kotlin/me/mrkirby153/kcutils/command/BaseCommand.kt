package me.mrkirby153.kcutils.command

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

/**
 * A base class for all commands
 *
 * @param instance      The [Plugin][JavaPlugin] owning the command
 * @param name          The name of the command
 * @param permission    The permission node required to run the command
 * @param aliases       A list of aliases for this command
 */
abstract class BaseCommand<T : JavaPlugin>(protected val instance: T, val name: String, val permission: String?, private val aliases: Array<String>) {

    /**
     * The alias used when this command was executed
     */
    protected var aliasUsed: String? = null
        private set

    constructor(instance: T, name: String, aliases: Array<String>) : this(instance, name, null, aliases)

    /**
     * Called when the command has been executed
     *
     * @param player    The player who executed the command
     * @param args      The arguments for the command
     */
    abstract fun execute(player: Player, args: Array<String>)

    /**
     * Checks if the provided command is an alias of the current command
     *
     * @param command   The command to check
     *
     * @return True if the provided command is the alias of the current command
     */
    fun isAlias(command: String): Boolean {
        return aliases.any { it.equals(command, ignoreCase = true) }
    }

    /**
     * Sets the alias used when executing this command
     *
     * @param alias The alias used
     */
    fun setAlasUsed(alias: String) {
        this.aliasUsed = alias
    }

    /**
     * Gets tab complete options for this command
     *
     * @param player    The player
     * @parma command   The command being executed
     * @param args      The arguments being passed
     */
    open fun tabComplete(player: Player, command: String, args: Array<String>): List<String> {
        return ArrayList()
    }
}
