package me.mrkirby153.kcutils.scoreboard

import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.entity.Player
import java.util.UUID

/**
 * A team on a [Scoreboard][KirbyScoreboard]
 *
 * @param teamName  The name of the team
 * @param color     The color of the team
 */
open class ScoreboardTeam(val teamName: String, var color: NamedTextColor) {

    /**
     * A list of players on the scoreboard
     */
    val players = mutableSetOf<UUID>()

    var prefixColor = color

    /**
     * The team name truncated to 16 chars, used for internal reference
     */
    val filteredName: String
        get() = teamName.substring(0, Math.min(teamName.length, 16))

    /**
     * If the team's prefix should be shown
     */
    var showPrefix = false

    /**
     * The prefix to show
     */
    var prefix: String? = null

    /**
     * The format of the prefix
     */
    var prefixFormat = "[%s]"

    /**
     * If friendly fire is enabled
     */
    var friendlyFire = false

    /**
     * If the players of the team can see each other if invisible
     */
    var seeInvisible = false

    override fun equals(other: Any?): Boolean {
        return other is ScoreboardTeam && other.teamName == this.teamName
    }

    override fun hashCode(): Int {
        var result = teamName.hashCode()
        result = 31 * result + color.hashCode()
        result = 31 * result + prefixColor.hashCode()
        result = 31 * result + showPrefix.hashCode()
        result = 31 * result + (prefix?.hashCode() ?: 0)
        result = 31 * result + friendlyFire.hashCode()
        result = 31 * result + seeInvisible.hashCode()
        return result
    }

    /**
     * Removes a player from the team
     *
     * @param player The player to remove
     */
    open fun removePlayer(player: Player) {
        players.remove(player.uniqueId)
    }

    /**
     * Adds a player to the team
     *
     * @param player The player to add to the team
     */
    open fun addPlayer(player: Player) {
        players.add(player.uniqueId)
    }

    /**
     * Removes a player from the team
     *
     * @param uuid  The user to remove from the team
     */
    fun removePlayer(uuid: UUID) {
        players.remove(uuid)
    }


    /**
     * Adds a player to the team
     *
     * @param uuid The user to add to the team
     */
    fun addPlayer(uuid: UUID) {
        players.add(uuid)
    }
}