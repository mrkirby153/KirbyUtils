package me.mrkirby153.kcutils.command

import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

abstract class BaseCommand<T : JavaPlugin>(protected val instance: T, val name: String, val permission: String?, private val aliases: Array<String>) {

    protected var aliasUsed: String? = null
        private set

    constructor(instance: T, name: String, aliases: Array<String>) : this(instance, name, null, aliases)

    abstract fun execute(player: Player, args: Array<String>)

    fun isAlias(command: String): Boolean {
        return aliases.any { it.equals(command, ignoreCase = true) }
    }

    fun setAlasUsed(alias: String) {
        this.aliasUsed = alias
    }

    fun tabComplete(player: Player, command: String, args: Array<String>): List<String> {
        return ArrayList()
    }
}
