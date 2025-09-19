package me.mrkirby153.kcutils.cooldown

import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Module
import me.mrkirby153.kcutils.Time
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
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
@Deprecated("Use ItemCooldownManager instead")
class CooldownManager(plugin: JavaPlugin) : Module<JavaPlugin>("cooldown", plugin), Runnable {

    /**
     * A map of materials to the cooldown they represent
     */
    private val cooldownDisplays = mutableMapOf<Material, Cooldown<UUID>>()

    /**
     * A list of cooldowns that will notify the player
     */
    private val notifiable = mutableListOf<Cooldown<UUID>>()

    override fun run() {
        Bukkit.getOnlinePlayers().forEach {
            val itemInHand = it.inventory.itemInMainHand

            val material = itemInHand.type

            val cooldown = cooldownDisplays[material] ?: return@forEach

            val time = cooldown.getTimeLeft(it.uniqueId)

            if (cooldown.check(it.uniqueId)) {
                return@forEach
            }

            val actionBar =
                Component.text("[ ").append(buildBar(1 - cooldown.getPercentComplete(it.uniqueId)))
                    .append(Component.text("] ")).append(
                        Component.text(Time.format(1, time, smallest = Time.TimeUnit.SECONDS))
                            .color(NamedTextColor.GOLD)
                    )
            it.sendActionBar(actionBar)
        }
        notifiable.forEach { cooldown ->
            cooldown.getPendingNotifcations().mapNotNull { Bukkit.getPlayer(it) }.forEach {
                if (cooldown.getPendingNotifcations().contains(
                        it.uniqueId
                    ) && cooldown.notify && cooldown.check(it.uniqueId)
                ) {
                    cooldown.removeNotifcation(it.uniqueId)
                    it.sendMessage(
                        Chat.message(
                            "Cooldown",
                            "{name} recharged!",
                            "name" to cooldown.name
                        )
                    )
                    it.playSound(
                        it.location,
                        Sound.BLOCK_NOTE_BLOCK_PLING,
                        SoundCategory.MASTER,
                        1F,
                        2F
                    )
                    it.sendActionBar(
                        Component.text(cooldown.name, NamedTextColor.AQUA)
                            .append(Component.text(" recharged!", NamedTextColor.GREEN))
                    )
                }
            }
        }
    }

    override fun init() {
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
    fun unregisterNotifiable(cooldown: Cooldown<UUID>) {
        this.notifiable.remove(cooldown)
    }

    /**
     * Unregisters a cooldown from being displayed
     *
     * @param material  The material previously triggering the cooldown
     */
    fun removeCooldown(material: Material) {
        cooldownDisplays.remove(material)
    }

    /**
     * Construct the cooldown progress bar
     *
     * @param percent       The percent full the bar is
     * @param segments      The amount of segments in the bar
     * @param filledColor   The color of filled segments
     * @param emptyColor    The color of empty segments
     *
     * @return A [Component] of the built bar
     */
    @JvmOverloads
    fun buildBar(
        percent: Double, segments: Int = 10, segmentChar: Char = '█',
        filledColor: TextColor = NamedTextColor.GREEN,
        emptyColor: TextColor = NamedTextColor.RED
    ): Component {
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
        return Component.text(filled).color(filledColor)
            .append(Component.text(empty).color(emptyColor))
    }

}