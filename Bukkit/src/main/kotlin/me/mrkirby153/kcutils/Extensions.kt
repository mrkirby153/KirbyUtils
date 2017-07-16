package me.mrkirby153.kcutils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player

/**
 * Sends a BaseComponent to the given player
 */
fun Player.sendMessage(component: BaseComponent) = this.spigot().sendMessage(component)


@JvmOverloads
fun component(text: String? = null, color: ChatColor = ChatColor.WHITE): TextComponentBuilder = TextComponentBuilder().apply {
    if (text != null)
        this.text = text
    this.color = color
}

inline fun component(text: String? = null, value: TextComponentBuilder.() -> Unit): TextComponentBuilder {
    return component(text).apply(value)
}

inline fun component(text: String? = null, color: ChatColor, value: TextComponentBuilder.() -> Unit): TextComponentBuilder {
    return component(text, color).apply(value)
}

fun itemStack(material: Material, data: Int = 0): ItemStackBuilder = ItemStackBuilder(material, data)

inline fun itemStack(material: Material, value: ItemStackBuilder.() -> Unit): ItemStackBuilder {
    return itemStack(material).apply(value)
}

inline fun itemStack(material: Material, data: Int, value: ItemStackBuilder.() -> Unit): ItemStackBuilder = itemStack(material, data).apply(value)

/**
 * Checks if a block is safe for players to teleport to
 * Criteria: Not a liquid or not below a liquid; This and the block above are air
 */
fun Block.safeToTeleport(): Boolean {
    // If this block is a liquid
    if (this.isLiquid || this.getRelative(org.bukkit.block.BlockFace.DOWN).isLiquid)
        return false
    if (this.type != Material.AIR || this.getRelative(org.bukkit.block.BlockFace.UP) != Material.AIR)
        return false
    return true
}