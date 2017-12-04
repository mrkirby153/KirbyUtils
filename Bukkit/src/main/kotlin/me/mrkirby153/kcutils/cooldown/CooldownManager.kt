package me.mrkirby153.kcutils.cooldown

import me.mrkirby153.kcutils.ActionBar
import me.mrkirby153.kcutils.Module
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.plugin.java.JavaPlugin
import java.util.*

class CooldownManager(plugin: JavaPlugin) : Module<JavaPlugin>("cooldown", plugin), Runnable {

    private val cooldownDisplays = mutableMapOf<Material, Cooldown<UUID>>()

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

    fun displayCooldown(material: Material, cooldown: Cooldown<UUID>) {
        cooldownDisplays[material] = cooldown
    }

    fun removeCooldown(material: Material) {
        cooldownDisplays.remove(material);
    }

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