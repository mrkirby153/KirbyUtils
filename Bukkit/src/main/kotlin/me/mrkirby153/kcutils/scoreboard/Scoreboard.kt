package me.mrkirby153.kcutils.scoreboard

import me.mrkirby153.kcutils.extensions.runnable
import me.mrkirby153.kcutils.extensions.toComponent
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.plugin.Plugin
import org.bukkit.scheduler.BukkitTask
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import org.bukkit.scoreboard.Objective
import org.bukkit.scoreboard.Scoreboard
import org.bukkit.scoreboard.Team
import java.lang.ref.WeakReference
import java.util.UUID

@DslMarker
@Retention(AnnotationRetention.BINARY)
annotation class ScoreboardDslMarker


@ScoreboardDslMarker
fun scoreboard(plugin: Plugin, body: ScoreboardDsl.() -> Unit) =
    ScoreboardDsl(plugin).apply(body)

@ScoreboardDslMarker
@JvmName("pluginScoreboard")
fun Plugin.scoreboard(body: ScoreboardDsl.() -> Unit) = scoreboard(this, body)


class ScoreboardDsl(internal val plugin: Plugin) {
    // A list of users who can view the scoreboard
    private val players = mutableListOf<WeakReference<Player>>()

    private val scoreboard = Bukkit.getScoreboardManager().newScoreboard.apply {
        val sidebar =
            this.registerNewObjective("sidebar", Criteria.DUMMY, Component.text("Uninitialized"))
        sidebar.displaySlot = DisplaySlot.SIDEBAR
    }

    private val objectives = mutableListOf<ObjectiveBuilder>()
    private val teams = mutableMapOf<String, TeamBuilder>()

    private val current = arrayOfNulls<Component>(15)

    private var titleUpdater: BukkitTask? = null
    private var lineUpdater: BukkitTask? = null

    private var titleHandler: () -> Component = { "Uninitialized".toComponent() }
    private var lineHandler: LineBuilder.() -> Unit = {}

    private var onInitialize: () -> Unit = {}

    private var initialized = false

    var titleUpdateInterval: Long = 0
        set(value) {
            field = value
            titleUpdater?.cancel()
            titleUpdater = null
            maybeStartTitleUpdate()
        }

    var lineUpdateInterval: Long = 0
        set(value) {
            field = value
            lineUpdater?.cancel()
            lineUpdater = null
            maybeStartLineUpdate()
        }


    fun destroy() {
        titleUpdater?.cancel()
        lineUpdater?.cancel()

        objectives.forEach {
            it.destroy()
        }

        // Reset everyone's scoreboard to the main scoreboard
        players.mapNotNull { it.get() }.forEach { p ->
            p.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
        }
    }

    fun show(player: Player) {
        val ref = WeakReference(player)
        players.add(ref)

        maybeInitialize()

        player.scoreboard = this.scoreboard

        maybeStartTitleUpdate()
        maybeStartLineUpdate()
        objectives.forEach { it.maybeStartUpdate() }
    }

    @ScoreboardDslMarker
    fun title(updater: () -> Component) {
        this.titleHandler = updater
    }

    fun title(component: Component) {
        title {
            component
        }
    }

    @ScoreboardDslMarker
    fun lines(handler: LineBuilder.() -> Unit) {
        this.lineHandler = handler
    }

    @ScoreboardDslMarker
    fun onInitialize(body: () -> Unit) {
        this.onInitialize = body
    }

    @ScoreboardDslMarker
    fun team(name: String, body: TeamBuilder.() -> Unit) {
        val builder = this.teams.computeIfAbsent(name) {
            TeamBuilder(
                scoreboard,
                scoreboard.registerNewTeam(it)
            )
        }
        builder.body()
    }

    @ScoreboardDslMarker
    fun objective(
        name: String,
        criteria: Criteria,
        slot: DisplaySlot,
        body: ObjectiveBuilder.() -> Unit = {}
    ) {
        check(slot != DisplaySlot.SIDEBAR) { "Cannot override the SIDEBAR objective" }
        val obj = scoreboard.registerNewObjective(name, criteria, null)
        obj.displaySlot = slot
        val objective = ObjectiveBuilder(this, obj).apply(body)
        this.objectives.add(objective)
        objective.maybeStartUpdate()
    }

    fun updateTitle() {
        val newTitle = titleHandler.invoke()
        val objective = scoreboard.getObjective(DisplaySlot.SIDEBAR) ?: return
        if (objective.displayName() != newTitle) {
            objective.displayName(newTitle)
        }
    }

    fun updateLines() {
        val builder = LineBuilder()
        val objective = scoreboard.getObjective(DisplaySlot.SIDEBAR) ?: return
        builder.apply(lineHandler)

        val existing = mutableMapOf<Int, String>()
        scoreboard.entries.forEach { line ->
            val score = objective.getScore(line)
            if (score.isScoreSet) {
                existing[score.score] = line
            }
        }

        val updatedKeys = mutableSetOf<Int>()
        val lines = mutableListOf<String>()
        builder.lines.forEach { (score, component) ->
            val existingLine = existing[score]
            var newLine = LegacyComponentSerializer.legacySection().serialize(component.invoke())
            // Allow duplicate lines to display
            while (newLine in lines) {
                newLine = "${ChatColor.RESET}$newLine"
            }
            if (existingLine != newLine) {
                if (existingLine != null) {
                    // Clear the old score
                    scoreboard.resetScores(existingLine)
                }
                objective.getScore(newLine).score = score
            }
            updatedKeys.add(score)
            lines.add(newLine)
        }

        existing.filterKeys { it !in updatedKeys }.forEach { (_, line) ->
            println("Resetting score for $line")
            scoreboard.resetScores(line)
        }
    }

    fun hide(player: Player) {
        players.removeIf { it.get() == null || it.get() == player }
        player.scoreboard = Bukkit.getScoreboardManager().mainScoreboard
    }

    private fun maybeInitialize() {
        if (initialized) {
            return
        }
        updateTitle()
        updateLines()
        try {
            onInitialize.invoke()
        } catch (e: Exception) {
            e.printStackTrace()
            plugin.logger.severe("Caught exception from onInitialize: ${e.message}")
        }
        initialized = true
    }


    internal fun hasPlayers() = players.any {
        val p = it.get()
        if (p != null) {
            p in Bukkit.getOnlinePlayers()
        } else {
            false
        }
    }

    private fun maybeStartTitleUpdate() {
        if (titleUpdater != null)
            return
        if (titleUpdateInterval > 0 && hasPlayers()) {
            titleUpdater = runnable { updateTitle() }.runTaskTimer(plugin, 0, titleUpdateInterval)
        }
    }

    private fun maybeStartLineUpdate() {
        if (lineUpdater != null)
            return
        if (lineUpdateInterval > 0 && hasPlayers()) {
            lineUpdater = runnable { updateLines() }.runTaskTimer(plugin, 0, lineUpdateInterval)
        }
    }


}

class LineBuilder {
    internal var lines = mutableMapOf<Int, () -> Component>()

    @ScoreboardDslMarker
    fun line(line: Int, body: () -> Component) {
        check(line > 0) { "No more space on the scoreboard" }
        lines[line] = body
    }

    fun line(body: () -> Component) {
        line(getNextLineNumber(), body)
    }

    fun line(line: Int, component: Component) {
        line(line) {
            component
        }
    }

    fun line(component: Component = Component.text("")) {
        line(getNextLineNumber(), component)
    }

    private fun getNextLineNumber(): Int {
        if (lines.isEmpty()) {
            return 15
        }
        return lines.keys.min() - 1
    }
}

class ObjectiveBuilder(private val scoreboard: ScoreboardDsl, private val objective: Objective) {

    private var updateTask: BukkitTask? = null

    private var updateHandler: Objective.() -> Unit = {}

    var updateInterval: Long = 0
        set(value) {
            field = value
            updateTask?.cancel()
            updateTask = null
            maybeStartUpdate()
        }

    @ScoreboardDslMarker
    fun onUpdate(body: Objective.() -> Unit) {
        this.updateHandler = body
    }

    private fun update() {
        this.updateHandler.invoke(objective)
    }

    fun displayName(component: Component) {
        objective.displayName(component)
    }

    fun maybeStartUpdate() {
        if (updateTask != null)
            return
        if (updateInterval > 0 && scoreboard.hasPlayers()) {
            println("Starting update at $updateInterval")
            updateTask = runnable { update() }.runTaskTimer(scoreboard.plugin, 0, updateInterval)
        }
    }

    fun destroy() {
        updateTask?.cancel()
    }
}

class TeamBuilder(private val scoreboard: Scoreboard, private val team: Team) {

    private val members = mutableSetOf<UUID>()

    var canSeeFriendlyInvisibles: Boolean = false
        set(value) {
            field = value
            update()
        }

    var friendlyFire: Boolean = true
        set(value) {
            field = value
            update()
        }

    var color: NamedTextColor = NamedTextColor.WHITE
        set(value) {
            field = value
            update()
        }


    var prefix: Component? = null
        set(value) {
            field = value
            update()
        }

    var suffix: Component? = null
        set(value) {
            field = value
            update()
        }

    fun add(player: Player) {
        members.add(player.uniqueId)
        update()
    }

    fun remove(player: Player) {
        members.remove(player.uniqueId)
        update()
    }

    fun members(vararg player: Player) {
        members.clear()
        members.addAll(player.map { it.uniqueId })
    }

    fun members(collection: Collection<Player>) {
        members.clear()
        members.addAll(collection.map { it.uniqueId })
    }

    fun getMembers() = members.map { Bukkit.getOfflinePlayer(it) }

    private fun update() {
        team.prefix(prefix?.run {
            append(" ".toComponent())
        })
        team.suffix(suffix?.run {
            Component.text(" ").append(this)
        })
        team.color(color)
        team.setCanSeeFriendlyInvisibles(canSeeFriendlyInvisibles)
        team.setAllowFriendlyFire(friendlyFire)

        val playersOnTeam = members.mapNotNull { Bukkit.getPlayer(it) }.map { it.name }
        val playersOnScoreboardTeam = team.entries

        val toAdd = playersOnTeam.filter { p -> p !in playersOnScoreboardTeam }
        val toRemove = playersOnScoreboardTeam.filter { p -> p !in playersOnTeam }
        team.removeEntries(toRemove)
        team.addEntries(toAdd)
    }
}