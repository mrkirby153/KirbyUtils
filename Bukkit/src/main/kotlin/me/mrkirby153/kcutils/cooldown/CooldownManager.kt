package me.mrkirby153.kcutils.cooldown

import me.mrkirby153.kcutils.ActionBar
import me.mrkirby153.kcutils.Module
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

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
     * A reference to the action bar module for working with action bars
     */
    private val actionBar: ActionBar = ActionBar(plugin)

    override fun run() {
        Bukkit.getOnlinePlayers().forEach {
            val itemInHand = it.inventory.itemInMainHand

            val material = itemInHand?.type

            val cooldown = cooldownDisplays[material]

            if(cooldown == null || material == null){
                actionBar.clear(it.player)
                return@forEach
            }

            val time = cooldown.getTimeLeft(it.uniqueId)

            if (cooldown.check(it.uniqueId)) {
                actionBar.clear(it.player)
                return@forEach
            }

            val component = TextComponent("[ ").apply {
                this.addExtra(buildBar(1-cooldown.getPercentComplete(it.uniqueId)))
                this.addExtra(" ] ")
                this.addExtra(me.mrkirby153.kcutils.Time.format(1, time, me.mrkirby153.kcutils.Time.TimeUnit.FIT))
            }

            actionBar[it] = component
        }
    }

    override fun init() {
        actionBar.load()
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
                 filledColor: ChatColor = ChatColor.GREEN, emptyColor: ChatColor = ChatColor.RED): TextComponent {
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