package me.mrkirby153.kcutils.scoreboard

import me.mrkirby153.kcutils.scoreboard.items.ElementText
import net.kyori.adventure.text.Component
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.util.Random

/**
 * Wrapper class for quickly creating scoreboards
 *
 * @param displayName The name of a scoreboard
 */
open class KirbyScoreboard(displayName: Component) {

    private val r = Random()

    constructor(displayName: String) : this(Component.text(displayName))

    /**
     * Gets the scoreboard
     *
     * @return The scoreboard
     */
    val board: Scoreboard = Bukkit.getScoreboardManager().newScoreboard
    private val sideObjective = addObjective(displayName, DisplaySlot.SIDEBAR)
    private val scoreboardElements = ArrayList<ScoreboardElement>()

    /**
     * A list of teams currently registered on the scoreboard
     */
    private val teams = HashSet<ScoreboardTeam>()

    /**
     * The current list of lines in the scoreboard
     */
    private val current = arrayOfNulls<String>(15)

    private var debug = false

    /**
     * Adds a scoreboard element
     *
     * @param element The scoreboard element to add
     */
    fun add(element: ScoreboardElement) {
        scoreboardElements.add(element)
    }

    /**
     * Adds a scoreboard element
     *
     * @param text The text to add
     */
    fun add(text: String) {
        this.add(ElementText(text))
    }

    /**
     * Adds a team to the scoreboard
     *
     * @param team The team to add
     */
    fun addTeam(team: ScoreboardTeam) {
        teams.add(team)
    }

    /**
     * Renders the scoreboard
     */
    fun draw() {
        if (debug) {
            println("--------------")
            println("Calling preDraw()")
        }
        preDraw()
        if (debug)
            println("Calling updateTeams()")
        updateTeams()

        if (debug)
            println("Updating scoreboard elements")
        val newLines = ArrayList<String>()
        for (item in this.scoreboardElements) {
            for (line in item.lines) {
                var l = line
                while (true) {
                    var matched = false
                    for (otherLines in newLines) {
                        if (l == otherLines) {
                            l += ChatColor.RESET
                            matched = true
                        }
                    }
                    if (!matched)
                        break
                }
                newLines.add(l)
            }
        }

        val toAdd = HashSet<Int>()
        val toRemove = HashSet<Int>()
        for (i in 0..14) {
            if (i >= newLines.size) {
                if (current[i] != null) {
                    toRemove.add(i)
                }
                continue
            }

            if (current[i] == null || current[i] != newLines[i]) {
                toRemove.add(i)
                toAdd.add(i)
            }
        }
        for (i in toRemove) {
            if (current[i] != null) {
                resetScore(current[i]!!)
                current[i] = null
            }
        }
        for (i in toAdd) {
            val newLine = newLines[i]
            sideObjective.getScore(newLine).score = 15 - i
            current[i] = newLine
        }

        if (debug)
            println("Calling postDraw()")
        postDraw()
    }

    /**
     * Enable debug output
     */
    fun enableDebug() {
        debug = true
    }

    /**
     * Get the teams registered on the scoreboard
     *
     * @return The teams registered on the scoreboard
     */
    open fun getTeams(): Set<ScoreboardTeam> {
        return teams
    }

    /**
     * Removes a team from the scoreboard
     *
     * @param team The team to remove
     */
    fun remove(team: ScoreboardTeam) {
        teams.remove(team)
    }

    /**
     * Resets the scoreboard (Clears all the lines)
     */
    fun reset() {
        scoreboardElements.clear()
    }

    /**
     * Clears the score of a line on the scoreboard (Removes it)
     *
     * @param line The line to remove
     */
    private fun resetScore(line: String) {
        board.resetScores(line)
    }

    /**
     * Updates the teams on the scoreboard
     */
    private fun updateTeams() {
        getTeams().forEach { t ->
            if (debug)
                println("Updating team " + t.teamName)
            var scoreboardTeam: Team? = null
            if (board.getTeam(t.filteredName) != null) {
                scoreboardTeam = board.getTeam(t.filteredName)
            }
            if (scoreboardTeam == null)
                scoreboardTeam = board.registerNewTeam(t.filteredName)

            val prefix = if (t.showPrefix) t.prefix?.uppercase() ?: "" else ""
            scoreboardTeam.prefix(Component.empty().color(t.prefixColor).content(prefix))
            scoreboardTeam.suffix(Component.empty())
            scoreboardTeam.setCanSeeFriendlyInvisibles(t.seeInvisible)
            scoreboardTeam.setAllowFriendlyFire(t.friendlyFire)
            scoreboardTeam.color(t.color)

            // Update all the players on the team
            val playersOnTeam = t.players.mapNotNull { Bukkit.getPlayer(it) }.map { it.name }
            val playersOnScoreboardTeam = scoreboardTeam.entries

            val toAdd = playersOnTeam.filter { p -> !playersOnScoreboardTeam.contains(p) }
            val toRemove = playersOnScoreboardTeam.filter { p -> !playersOnTeam.contains(p) }
            if (debug && toAdd.isNotEmpty())
                println("[ADD] " + toAdd.toTypedArray().contentToString())
            if (debug && toRemove.isNotEmpty())
                println("[REMOVE] " + toRemove.toTypedArray().contentToString())

            toRemove.forEach { scoreboardTeam.removeEntry(it) }
            toAdd.forEach { scoreboardTeam.addEntry(it) }
        }
        // Remove teams that no longer exist
        board.teams.forEach { team ->
            val exists = getTeams().any { it.teamName.startsWith(team.name) }
            if (!exists) {
                if (debug)
                    println("Unregistering team " + team.name)
                team.unregister()
            }
        }
    }

    /**
     * Adds an objective to the scoreboard
     *
     * @param displayName The display name of the objective
     * @param displaySlot The slot that the objective is displayed in
     * @param criteria    The criteria of the objective
     *
     * @return The [Objective] that was added
     */
    @JvmOverloads
    fun addObjective(
        displayName: Component,
        displaySlot: DisplaySlot,
        criteria: Criteria = Criteria.DUMMY
    ): Objective {
        val o = board.registerNewObjective("Obj-" + r.nextInt(999999), criteria, displayName)
        o.displaySlot = displaySlot
        return o
    }

    /**
     * Called after the scoreboard has been rendered
     */
    open fun postDraw() {

    }

    /**
     * Called before the scoreboard is rendered
     */
    open fun preDraw() {

    }

}
