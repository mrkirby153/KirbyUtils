package me.mrkirby153.kcutils.testplugin

import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Time
import me.mrkirby153.kcutils.extensions.italic
import me.mrkirby153.kcutils.extensions.miniMessage
import me.mrkirby153.kcutils.extensions.setScore
import me.mrkirby153.kcutils.extensions.toComponent
import me.mrkirby153.kcutils.scoreboard.ScoreboardDsl
import me.mrkirby153.kcutils.scoreboard.scoreboard
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.util.UUID

class TestPlugin : JavaPlugin(), Listener {

    private val scoreboards = mutableMapOf<UUID, ScoreboardDsl>()

    override fun onEnable() {
        val start = System.currentTimeMillis()
        logger.info("Hello, World!")
        val end = System.currentTimeMillis()
        logger.info("Initialized in ${Time.format(1, end - start)}")
        getCommand("test-command")?.setExecutor { sender, command, label, args ->
            if (sender !is Player) {
                sender.sendMessage(Chat.error("You must be a player to perform this command"))
                return@setExecutor true
            }
            if (args.isEmpty()) {
                val scoreboardForUser = getScoreboardForUser(sender)
                scoreboardForUser.show(sender)
                scoreboardForUser.team("Test") {
                    println("yay")
                    add(sender)
                }
            } else {
                if (args.size == 1 && args[0] == "destroy") {
                    scoreboards.remove(sender.uniqueId)?.destroy()
                    return@setExecutor true
                }
                clearScoreboard(sender)
            }
            true
        }
    }

    fun getScoreboardForUser(player: Player): ScoreboardDsl {
        if (scoreboards.contains(player.uniqueId))
            return scoreboards[player.uniqueId]!!
        var count = 0
        return scoreboard {
            lineUpdateInterval = 1
//            titleUpdateInterval = 1
            title {
                count += 1
                miniMessage("<rainbow>Scoreboard $count")
            }
            lines {
                line("Hello".toComponent())
                line("This is a scoreboard".toComponent())
                val location = player.location
                line(miniMessage("You are at <green>${location.blockX}</green> <red>${location.blockY}</red> <yellow>${location.blockZ}</yellow>"))
            }
            onInitialize {
                objective("health", Criteria.DUMMY, DisplaySlot.PLAYER_LIST) {
                    updateInterval = 20
                    onUpdate {
                        Bukkit.getOnlinePlayers().forEach { p ->
                            setScore(p, p.location.blockY)
                        }
                    }
                }
                team("Test") {
                    color = NamedTextColor.GREEN
                    canSeeFriendlyInvisibles = true
                    prefix = Component.text("[PREFIX]").color(NamedTextColor.GRAY)
                    suffix = Component.text("[SUFFIX]").color(NamedTextColor.YELLOW).italic()
                }
            }
        }.also {
            scoreboards[player.uniqueId] = it
        }
    }


    fun clearScoreboard(player: Player) {
        val existing = scoreboards.remove(player.uniqueId) ?: return
        existing.hide(player)
    }
}