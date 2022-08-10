package me.mrkirby153.kcutils.test

import me.mrkirby153.kcutils.scoreboard.KirbyScoreboard
import me.mrkirby153.kcutils.scoreboard.ScoreboardTeam
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import java.util.UUID

class TestScoreboard(val player: Player) : KirbyScoreboard("Test Scoreboard") {

    val team: ScoreboardTeam = ScoreboardTeam("Test Team", NamedTextColor.AQUA)

    init {
        team.prefix = "Testing"
        team.showPrefix = true
        team.addPlayer(player)
    }

    fun update() {
        this.reset()
        this.add("" + ChatColor.GOLD + ChatColor.BOLD + "Test Scoreboard")
        this.add("" + ChatColor.GREEN + "X: ${player.location.blockX}")
        this.add("" + ChatColor.YELLOW + "Y: ${player.location.blockY}")
        this.add("" + ChatColor.RED + "Z: ${player.location.blockZ}")

        if (player.scoreboard != board) {
            player.scoreboard = board
        }
        this.draw()
    }

    override fun getTeams(): Set<ScoreboardTeam> {
        return mutableSetOf(team)
    }

    companion object {
        val scoreboards = mutableMapOf<UUID, TestScoreboard>()


        fun setScoreboard(player: Player) {
            val sb = TestScoreboard(player)
            scoreboards[player.uniqueId] = sb
            player.scoreboard = sb.board
        }

        fun unsetScoreboard(player: Player) {
            scoreboards.remove(player.uniqueId)
        }
    }
}