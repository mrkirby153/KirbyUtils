package me.mrkirby153.kcutils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player

/**
 * Sends a BaseComponent to the given player
 */
fun Player.sendMessage(component: BaseComponent) = this.spigot().sendMessage(component)


/**
 * Creates a [TextComponentBuilder]
 *
 * @param text  The Text
 * @param color The color of the text
 *
 * @return A [TextComponentBuilder]
 */
@JvmOverloads
fun component(text: String? = null,
              color: ChatColor = ChatColor.WHITE): TextComponentBuilder = TextComponentBuilder().apply {
    if (text != null)
        this.text = text
    this.color = color
}

/**
 * Creates a [TextComponentBuilder]
 *
 * @param   text The text
 * @param   value The builder to apply
 *
 * @return A [TextComponentBuilder]
 */
inline fun component(text: String? = null,
                     value: TextComponentBuilder.() -> Unit): TextComponentBuilder {
    return component(text).apply(value)
}

/**
 * Creates a [TextComponentBuilder]
 *
 * @param   text The text
 * @param   color The color of the text
 * @param   value The builder to apply
 *
 * @return A [TextComponentBuilder]
 */
inline fun component(text: String? = null, color: ChatColor,
                     value: TextComponentBuilder.() -> Unit): TextComponentBuilder {
    return component(text, color).apply(value)
}

/**
 * Creates a new [ItemStackBuilder]
 *
 * @param material  The material of the item
 * @param data      The data/damage value of the item
 *
 * @return An [ItemStackBuilder]
 */
fun itemStack(material: Material, data: Int = 0): ItemStackBuilder = ItemStackBuilder(material,
        data)

/**
 * Creates a new [ItemStackBuilder]
 *
 * @param material  The material of the item
 * @param value     The builder to apply
 *
 * @return An [ItemStackBuilder]
 */
inline fun itemStack(material: Material, value: ItemStackBuilder.() -> Unit): ItemStackBuilder {
    return itemStack(material).apply(value)
}

/**
 * Creates an new [ItemStackBuilder]
 *
 * @param material  The material of the item
 * @param data      The data of the item
 * @param value     The builder to apply
 *
 * @return An [ItemStackBuilder]
 */
inline fun itemStack(material: Material, data: Int,
                     value: ItemStackBuilder.() -> Unit): ItemStackBuilder = itemStack(material,
        data).apply(value)

/**
 * Checks if a block is safe for players to teleport to
 * Criteria: Not a liquid or not below a liquid; This and the block above are air
 *
 * @return True if the block is safe for a user to teleport to
 */
fun Block.safeToTeleport(): Boolean {
    // If this block is a liquid
    if (this.isLiquid || this.getRelative(org.bukkit.block.BlockFace.DOWN).isLiquid)
        return false
    if (this.type != Material.AIR || this.getRelative(
            org.bukkit.block.BlockFace.UP) != Material.AIR)
        return false
    return true
}

/**
 * Gets or creates the given configuration section
 *
 * @param section   The section to get
 *
 * @return The section
 */
fun YamlConfiguration.getOrCreateSection(section: String): ConfigurationSection {
    return this.getConfigurationSection(section) ?: this.createSection(section)
}
/**
 * Gets or creates the given configuration section
 *
 * @param section   The section to get
 *
 * @return The section
 */
fun ConfigurationSection.getOrCreateSection(section: String): ConfigurationSection {
    return this.getConfigurationSection(section) ?: this.createSection(section)
}