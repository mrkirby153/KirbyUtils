package me.mrkirby153.kcutils.cooldown

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.UseCooldown
import me.mrkirby153.kcutils.Module
import me.mrkirby153.kcutils.extensions.ticks
import net.kyori.adventure.key.Key
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import kotlin.time.Duration

class ItemCooldownManager(plugin: JavaPlugin) : Module<JavaPlugin>("itemcooldown", plugin),
    Runnable {

    private val cooldowns: MutableMap<Pair<UUID, Key>, Long> = mutableMapOf()

    private var taskId: Int? = null

    override fun run() {
        val iterator = cooldowns.iterator()
        while (iterator.hasNext()) {
            val entry = iterator.next()
            val (playerUuid, key) = entry.key
            val expireTime = entry.value
            if (System.currentTimeMillis() > expireTime) {
                iterator.remove()
                Bukkit.getPlayer(playerUuid)?.setCooldown(key, 0)
            }
        }
    }

    override fun init() {
        this.taskId = scheduleRepeating(0, 1, this)
    }

    override fun disable() {
        if (this.taskId != null) {
            cancelTask(this.taskId!!)
            this.taskId = null
        }
    }

    /**
     * Attach a provided [cooldown] to the given [itemStack]
     */
    fun attach(itemStack: ItemStack, cooldown: ItemCooldown) {
        itemStack.setData(
            DataComponentTypes.USE_COOLDOWN,
            UseCooldown.useCooldown(cooldown.time.inWholeSeconds.toFloat())
                .cooldownGroup(cooldown.key).build()
        )
    }

    /**
     * Uses the provided [cooldown] for the given [player]. Subsequent calls to [check] will return `false` until the cooldown expires
     */
    fun use(player: Player, cooldown: ItemCooldown): Boolean {
        return if (check(player, cooldown)) {
            player.setCooldown(cooldown.key, cooldown.time.ticks)
            log("Cooldown is ${cooldown.time} ticks")
            val expireTime = System.currentTimeMillis() + cooldown.time.inWholeMilliseconds
            cooldowns[getKey(cooldown, player)] = expireTime
            true
        } else {
            false
        }
    }

    /**
     * Resets a [cooldown] for a [player]
     */
    fun reset(player: Player, cooldown: ItemCooldown) {
        player.setCooldown(cooldown.key, 0)
        cooldowns.remove(getKey(cooldown, player))
    }

    /**
     * Gets the time when the cooldown will expire
     */
    fun get(player: Player, cooldown: ItemCooldown): Long? {
        return cooldowns[getKey(cooldown, player)]
    }

    /**
     * Checks if the provided [player] is permitted to use the given [cooldown]. Returns `true` if the
     * user is **allowed** to use this
     */
    fun check(player: Player, cooldown: ItemCooldown): Boolean {
        val time = cooldowns[getKey(cooldown, player)] ?: return true
        return System.currentTimeMillis() > time
    }

    private fun getKey(itemCooldown: ItemCooldown, player: Player) =
        Pair(player.uniqueId, itemCooldown.key)
}

/**
 * Represents an item cooldown.
 */
data class ItemCooldown(
    /**
     * The cooldown duration
     */
    val time: Duration,

    /**
     * The key of this cooldown
     */
    val key: Key
) {
    constructor(time: Duration, namespace: String, value: String) : this(
        time,
        Key.key(namespace, value)
    )

    constructor(time: Duration, value: String) : this(time, "kirbyutils", value)

    constructor(time: Duration) : this(time, UUID.randomUUID().toString().replace("-", ""))
}