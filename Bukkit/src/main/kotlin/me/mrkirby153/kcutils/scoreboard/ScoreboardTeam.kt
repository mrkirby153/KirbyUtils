package me.mrkirby153.kcutils.scoreboard

import net.md_5.bungee.api.ChatColor
import org.bukkit.entity.Player
import java.util.*

open class ScoreboardTeam(val teamName: String, val color: ChatColor) {

    val players = mutableSetOf<UUID>()
    val prefixColor = color

    val filteredName: String
        get() = teamName.substring(0, Math.min(teamName.length, 16))

    var showPrefix = false
    var prefix: String? = null
    var prefixFormat = "[%s]"

    var friendlyFire = false
    var seeInvisible = false

    override fun equals(other: Any?): Boolean {
        return other is ScoreboardTeam && other.teamName == this.teamName
    }

    override fun hashCode(): Int {
        var result = teamName.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + players.hashCode()
        result = 31 * result + prefixColor.hashCode()
        result = 31 * result + showPrefix.hashCode()
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + friendlyFire.hashCode()
        result = 31 * result + seeInvisible.hashCode()
        return result
    }

    open fun removePlayer(player: Player) {
        players.remove(player.uniqueId)
    }

    open fun addPlayer(player: Player) {
        players.add(player.uniqueId)
    }

    fun removePlayer(uuid: UUID) {
        players.remove(uuid)
    }

    fun addPlayer(uuid: UUID) {
        players.add(uuid)
    }
}