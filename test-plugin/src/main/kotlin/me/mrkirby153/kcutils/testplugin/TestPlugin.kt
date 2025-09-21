package me.mrkirby153.kcutils.testplugin

import co.aikar.commands.BaseCommand
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Time
import me.mrkirby153.kcutils.cooldown.ItemCooldown
import me.mrkirby153.kcutils.cooldown.ItemCooldownManager
import me.mrkirby153.kcutils.extensions.glowing
import me.mrkirby153.kcutils.extensions.italic
import me.mrkirby153.kcutils.extensions.itemStack
import me.mrkirby153.kcutils.extensions.meta
import me.mrkirby153.kcutils.extensions.miniMessage
import me.mrkirby153.kcutils.extensions.namespacedKey
import me.mrkirby153.kcutils.extensions.setScore
import me.mrkirby153.kcutils.extensions.toComponent
import me.mrkirby153.kcutils.gui.gui
import me.mrkirby153.kcutils.scoreboard.ScoreboardDsl
import me.mrkirby153.kcutils.scoreboard.scoreboard
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.persistence.PersistentDataType
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.scoreboard.Criteria
import org.bukkit.scoreboard.DisplaySlot
import java.util.UUID
import kotlin.time.Duration.Companion.seconds


val TEST_COOLDOWN = ItemCooldown(5.seconds, "test_cooldown")

class TestPlugin : JavaPlugin(), Listener {

    private val scoreboards = mutableMapOf<UUID, ScoreboardDsl>()
    private lateinit var commandManager: PaperCommandManager
    val cooldownManager = ItemCooldownManager(this)

    override fun onEnable() {
        val start = System.currentTimeMillis()
        logger.info("Hello, World!")

        commandManager = PaperCommandManager(this)
        commandManager.registerCommand(Commands(this))
        server.pluginManager.registerEvents(this, this)
        cooldownManager.init()

        val end = System.currentTimeMillis()
        logger.info("Initialized in ${Time.format(1, end - start)}")
    }


    fun getGui(player: Player) = gui(1, "Test GUI".toComponent()) {
        updateFrequency = 1
        slot(0, 0, itemStack(Material.SUNFLOWER) {
            displayName("Set Time To Day".toComponent().italic(false))
        }) {
            onClick {
                if (clickType == ClickType.LEFT) {
                    player.world.time = 0
                    close()
                }
            }
            onUpdate {
                item?.glowing = player.world.time < 10000
                item?.meta {
                    lore(
                        listOf(
                            miniMessage("<green>Current Time:</green> <yellow>${player.world.time}").italic(
                                false
                            )
                        )
                    )
                }
            }
        }
        slot(0, 1, itemStack(Material.RED_BED) {
            displayName("Set Time To Night".toComponent().italic(false))
        }) {
            onClick {
                if (clickType == ClickType.LEFT) {
                    player.world.time = 23000
                    close()
                }
            }
        }
    }

    fun getScoreboardForUser(player: Player): ScoreboardDsl {
        if (scoreboards.contains(player.uniqueId))
            return scoreboards[player.uniqueId]!!
        var count = 0
        return scoreboard {
            enableNumbers()
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
                }.add(player)
            }
        }.also {
            scoreboards[player.uniqueId] = it
        }
    }


    fun clearScoreboard(player: Player) {
        val existing = scoreboards.remove(player.uniqueId) ?: return
        existing.hide(player)
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val scoreboard = getScoreboardForUser(event.player)
        scoreboard.show(event.player)
    }

    @EventHandler
    fun onLeave(event: PlayerQuitEvent) {
        clearScoreboard(event.player)
    }

    @EventHandler(ignoreCancelled = true)
    fun onPlayerInteract(event: PlayerInteractEvent) {
        val item = event.player.inventory.itemInMainHand
        if (event.action != Action.RIGHT_CLICK_BLOCK) {
            return
        }
        if (item.persistentDataContainer.get(
                this.namespacedKey("type"),
                PersistentDataType.STRING
            ) != "test_cooldown_item"
        ) {
            return
        }
        if (cooldownManager.use(event.player, TEST_COOLDOWN)) {
            event.player.sendMessage { Chat.message("Cooldown", "You've used the cooldown!") }
        } else {
            val until = cooldownManager.get(event.player, TEST_COOLDOWN)!!
            val duration = until - System.currentTimeMillis()
            event.player.sendMessage {
                Chat.message(
                    "Cooldown",
                    "This item is on cooldown for {time}!",
                    "time" to Time.format(1, duration)
                )
            }
        }
    }

}

@CommandAlias("test-command")
class Commands(private val plugin: TestPlugin) : BaseCommand() {

    @Subcommand("gui")
    fun gui(player: Player) {
        plugin.getGui(player).open(player)
    }

    @Subcommand("chat-test")
    fun chatTest(sender: CommandSender) {
        val component1 =
            Chat.formattedChat("This is a test message", NamedTextColor.GREEN, TextDecoration.BOLD)
                .append(
                    Chat.formattedChat(" This is another message", NamedTextColor.YELLOW)
                )
        val component2 =
            Chat.formattedChat("This is a third message", NamedTextColor.BLUE, TextDecoration.BOLD)
                .append(
                    Chat.formattedChat(" This is the final message", NamedTextColor.YELLOW, false)
                )
        sender.sendMessage(component1)
        sender.sendMessage(component2)
    }

    @Subcommand("cooldown")
    fun cooldown(sender: Player) {
        val item = itemStack(Material.COMPASS)
        item.editPersistentDataContainer { container ->
            container.set(
                plugin.namespacedKey("type"),
                PersistentDataType.STRING,
                "test_cooldown_item"
            )
        }
        item.glowing = true
        plugin.cooldownManager.attach(item, TEST_COOLDOWN)
        sender.give(item)
        sender.sendMessage(Chat.formattedChat("Here you go!", NamedTextColor.GRAY))
    }

    @Subcommand("cooldown-reset")
    fun cooldownReset(sender: Player) {
        plugin.cooldownManager.reset(sender, TEST_COOLDOWN)
        sender.sendMessage(Chat.message("Cooldown", "Cooldown reset!"))
    }
}