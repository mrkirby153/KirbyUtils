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
import org.bukkit.scoreboard.Team
import java.lang.ref.WeakReference
import java.util.UUID

/**
 * Marker annotation for the scoreboard DSL
 */
@DslMarker
@Retention(AnnotationRetention.BINARY)
internal annotation class ScoreboardDslMarker


/**
 * Creates a new scoreboard
 */
fun scoreboard(plugin: Plugin, body: ScoreboardDsl.() -> Unit) =
    ScoreboardDsl(plugin).apply(body)

/**
 * Creates a new scoreboard
 */
@JvmName("pluginScoreboard")
fun Plugin.scoreboard(body: ScoreboardDsl.() -> Unit) = scoreboard(this, body)

/**
 * Wrapper class handing scoreboard operations.
 */
@ScoreboardDslMarker
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

    private var titleUpdater: BukkitTask? = null
    private var lineUpdater: BukkitTask? = null

    private var titleHandler: () -> Component = { "Uninitialized".toComponent() }
    private var lineHandler: LineBuilder.() -> Unit = {}

    private var onInitialize: () -> Unit = {}

    private var initialized = false

    /**
     * The interval at which the title should update (in ticks). Set to `0` to disable updating.
     *
     * The title can be manually updated by calling [updateTitle]
     */
    var titleUpdateInterval: Long = 0
        set(value) {
            field = value
            titleUpdater?.cancel()
            titleUpdater = null
            maybeStartTitleUpdate()
        }

    /**
     * The interval at which scoreboard lines update (in ticks). Set to `0` to disable updating.
     *
     * Lines can be manually updated by calling [updateLines]
     */
    var lineUpdateInterval: Long = 0
        set(value) {
            field = value
            lineUpdater?.cancel()
            lineUpdater = null
            maybeStartLineUpdate()
        }


    /**
     * Destroys this scoreboard. Cancels the running update tasks and removes the scoreboard from
     * all players
     */
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

    /**
     * Shows this scoreboard to the provided [player].
     *
     * The first time the scoreboard is shown to any user, it will be initialized
     */
    fun show(player: Player) {
        val ref = WeakReference(player)
        players.add(ref)

        maybeInitialize()

        player.scoreboard = this.scoreboard

        maybeStartTitleUpdate()
        maybeStartLineUpdate()
        objectives.forEach { it.maybeStartUpdate() }
    }

    /**
     * Sets the title of this scoreboard. This method is invoked every time [updateTitle] is called
     */
    fun title(updater: () -> Component) {
        this.titleHandler = updater
    }

    /**
     * Sets the title of this scoreboard to the given [component].
     */
    fun title(component: Component) {
        title {
            component
        }
    }

    /**
     * Sets the lines of this scoreboard. This method is invoked every time [updateLines] is called.
     *
     * To prevent flickering of the scoreboard, only lines that have changed will be re-sent to
     * players
     */
    fun lines(handler: LineBuilder.() -> Unit) {
        this.lineHandler = handler
    }

    /**
     * Invoked once when this scoreboard is first shown. Perform any one-time set up here
     */
    fun onInitialize(body: () -> Unit) {
        this.onInitialize = body
    }

    /**
     * Gets or creates the team with the given [name] and returns a [TeamBuilder]
     */
    fun team(name: String, body: TeamBuilder.() -> Unit) {
        val builder = this.teams.computeIfAbsent(name) {
            TeamBuilder(
                scoreboard.registerNewTeam(it)
            )
        }
        builder.body()
    }

    /**
     * Creates a new objective with the provided [name], [criteria], and [slot].
     *
     * [DisplaySlot.SIDEBAR] cannot be overridden by this method. Use [lines] instead.
     */
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

    /**
     * Updates the sidebar's title
     */
    fun updateTitle() {
        val newTitle = titleHandler.invoke()
        val objective = scoreboard.getObjective(DisplaySlot.SIDEBAR) ?: return
        if (objective.displayName() != newTitle) {
            objective.displayName(newTitle)
        }
    }

    /**
     * Updates the sidebar's lines
     */
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

    /**
     * Removes this scoreboard from the player. Their scoreboard will be reset to the main scoreboard
     */
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

/**
 * Builder for building a series of lines for the scoreboard.
 *
 * Each scoreboard can have a maximum of 16 lines. To prevent flickering, each line is compared with
 * the current scoreboard's lines, and only the line that's different will be changed. For example,
 * if one line out of 15 changes, that one line will be updated on the scoreboard, instead of all 15.
 *
 *
 * Additionally, LineBuilders can only support [NamedTextColor] due to Minecraft limitations
 */
@ScoreboardDslMarker
class LineBuilder {
    internal var lines = mutableMapOf<Int, () -> Component>()

    /**
     * Sets the line at [line] to the value returned by [body]
     */
    fun line(line: Int, body: () -> Component) {
        check(line > 0) { "No more space on the scoreboard" }
        lines[line] = body
    }

    /**
     * Sets the next line to the value returned by [body]
     */
    fun line(body: () -> Component) {
        line(getNextLineNumber(), body)
    }

    /**
     * Sets the line at [line] to [component]
     */
    fun line(line: Int, component: Component) {
        line(line) {
            component
        }
    }

    /**
     * Sets the next line to [component]
     */
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

/**
 * Builder for custom objectives on the scoreboard. The most commonly used objective slots are
 * [DisplaySlot.BELOW_NAME] and [DisplaySlot.PLAYER_LIST] to show objectives below the name and in
 * the player list respectively.
 */
@ScoreboardDslMarker
class ObjectiveBuilder(private val scoreboard: ScoreboardDsl, private val objective: Objective) {

    private var updateTask: BukkitTask? = null

    private var updateHandler: Objective.() -> Unit = {}

    /**
     * The interval [onUpdate] should be called (in ticks). Set to `0` to disable.
     */
    var updateInterval: Long = 0
        set(value) {
            field = value
            updateTask?.cancel()
            updateTask = null
            maybeStartUpdate()
        }

    /**
     * Invoked periodically according to [updateInterval] to update the objective's values
     */
    fun onUpdate(body: Objective.() -> Unit) {
        this.updateHandler = body
    }

    private fun update() {
        this.updateHandler.invoke(objective)
    }

    /**
     * Sets the display name of this objective to [component]
     */
    fun displayName(component: Component) {
        objective.displayName(component)
    }

    internal fun maybeStartUpdate() {
        if (updateTask != null)
            return
        if (updateInterval > 0 && scoreboard.hasPlayers()) {
            println("Starting update at $updateInterval")
            updateTask = runnable { update() }.runTaskTimer(scoreboard.plugin, 0, updateInterval)
        }
    }

    /**
     * Stops the periodic invocation of [onUpdate]
     */
    fun destroy() {
        updateTask?.cancel()
    }
}

/**
 * Builder for managing scoreboard teams
 */
@ScoreboardDslMarker
class TeamBuilder(private val team: Team) {

    private val members = mutableSetOf<UUID>()

    /**
     * If players on this team can see friendly invisble users
     */
    var canSeeFriendlyInvisibles: Boolean = false
        set(value) {
            field = value
            update()
        }

    /**
     * If friendly fire is enabled for this team
     */
    var friendlyFire: Boolean = true
        set(value) {
            field = value
            update()
        }

    /**
     * The team's color
     */
    var color: NamedTextColor = NamedTextColor.WHITE
        set(value) {
            field = value
            update()
        }


    /**
     * The team's prefix
     */
    var prefix: Component? = null
        set(value) {
            field = value
            update()
        }

    /**
     * The team's suffix
     */
    var suffix: Component? = null
        set(value) {
            field = value
            update()
        }

    /**
     * Adds the given [player] to the team
     */
    fun add(player: Player) {
        members.add(player.uniqueId)
        update()
    }

    /**
     * Removes the given [player] from the team
     */
    fun remove(player: Player) {
        members.remove(player.uniqueId)
        update()
    }

    /**
     * Sets the members of the team to [player]
     */
    fun members(vararg player: Player) {
        members.clear()
        members.addAll(player.map { it.uniqueId })
    }

    /**
     * Sets the members of the team to [members]
     */
    fun members(collection: Collection<Player>) {
        members.clear()
        members.addAll(collection.map { it.uniqueId })
    }

    /**
     * Get a list of all users on the team
     */
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