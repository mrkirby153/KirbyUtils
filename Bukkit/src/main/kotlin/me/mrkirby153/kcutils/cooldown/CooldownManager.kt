package me.mrkirby153.kcutils.cooldown

import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Module
import me.mrkirby153.kcutils.Time
import me.mrkirby153.kcutils.component
import me.mrkirby153.kcutils.protocollib.ProtocolLib
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID

/**
 * A module which handles cooldowns for a command
 *
 * @param plugin The owning plugin
 */
class CooldownManager(plugin: JavaPlugin) : Module<JavaPlugin>("cooldown", plugin), Runnable {

    /**
     * A map of materials to the cooldown they represent
     */
    private val cooldownDisplays = mutableMapOf<Material, Cooldown<UUID>>()

    /**
     * A list of cooldowns that will notify the player
     */
   private val notifiable = mutableListOf<Cooldown<UUID>>()

    /**
     * The ProtocolLib library used for handling packets
     */
    private val protocolLib: ProtocolLib = ProtocolLib(plugin)

    override fun run() {
        Bukkit.getOnlinePlayers().forEach {
            val itemInHand = it.inventory.itemInMainHand

            val material = itemInHand.type ?: return@forEach

            val cooldown = cooldownDisplays[material] ?: return@forEach

            val time = cooldown.getTimeLeft(it.uniqueId)

            if (cooldown.check(it.uniqueId)) {
                return@forEach
            }

            val component = TextComponent("[ ").apply {
                this.addExtra(buildBar(1 - cooldown.getPercentComplete(it.uniqueId)))
                this.addExtra(" ] ")
                this.addExtra(me.mrkirby153.kcutils.Time.format(1, time, Time.TimeUnit.FIT,
                        Time.TimeUnit.SECONDS))
            }

            this.protocolLib.sendActionBar(it, component)
        }
        notifiable.forEach { cooldown ->
            cooldown.getPendingNotifcations().mapNotNull { Bukkit.getPlayer(it) }.forEach { it ->
                if (cooldown.getPendingNotifcations().contains(
                        it.uniqueId) && cooldown.notify && cooldown.check(it.uniqueId)) {
                    cooldown.removeNotifcation(it.uniqueId)
                    it.spigot().sendMessage(
                            Chat.message("Cooldown", "{name} recharged!", "{name}", cooldown.name))
                    it.playSound(it.location, Sound.BLOCK_NOTE_BLOCK_PLING, SoundCategory.MASTER, 1F, 2F)
                    this.protocolLib.sendActionBar(it, component(cooldown.name) {
                        color = ChatColor.AQUA
                        bold = true
                        extra(component(" recharged!") {
                            color = ChatColor.GREEN
                            bold = true
                        })
                    }.build())
                }
            }
        }
    }

    override fun init() {
        protocolLib.init()
        scheduleRepeating(0, 1, this)
    }

    /**
     * Registers a cooldown to be displayed when the user has an item in their hand
     *
     * @param material  The material triggering the cooldown
     * @param cooldown  The cooldown to display
     */
    fun displayCooldown(material: Material, cooldown: Cooldown<UUID>) {
        cooldownDisplays[material] = cooldown
    }

    /**
     * Register a cooldown to display notifications
     *
     * @param cooldown The cooldown to register
     */
    fun registerNotifiable(cooldown: Cooldown<UUID>) {
        cooldown.notify = true
        this.notifiable.add(cooldown)
    }

    /**
     * Unregisters a cooldown from displaying notifications
     *
     * @param cooldown The cooldown to unregister
     */
    fun unregisterNotifiable(cooldown: Cooldown<UUID>){
        this.notifiable.remove(cooldown)
    }

    /**
     * Unregisters a cooldown from being displayed
     *
     * @param material  The material previously triggering the cooldown
     */
    fun removeCooldown(material: Material) {
        cooldownDisplays.remove(material);
    }

    /**
     * Construct the cooldown progress bar
     *
     * @param percent       The percent full the bar is
     * @param segments      The amount of segments in the bar
     * @param filledColor   The color of filled segments
     * @param emptyColor    The color of empty segments
     *
     * @return A [TextComponent] of the built bar
     */
    @JvmOverloads
    fun buildBar(percent: Double, segments: Int = 10, segmentChar: Char = '█',
                 filledColor: ChatColor = ChatColor.GREEN,
                 emptyColor: ChatColor = ChatColor.RED): TextComponent {
        val filledSegments = Math.floor(segments * percent).toInt()
        val emptySegments = segments - filledSegments

        val filled = buildString {
            for (i in 0 until filledSegments)
                append(segmentChar)
        }

        val empty = buildString {
            for (i in 0 until emptySegments)
                append(segmentChar)
        }
        return TextComponent(filled).apply {
            this.color = filledColor
            addExtra(TextComponent(empty).apply {
                this.color = emptyColor
            })
        }
    }

}