package me.mrkirby153.kcutils

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.file.YamlConfiguration

/**
 * Creates a new [ItemStackBuilder]
 *
 * @param material  The material of the item
 * @param data      The data/damage value of the item
 *
 * @return An [ItemStackBuilder]
 */
fun itemStack(material: Material, data: Int = 0): ItemStackBuilder = ItemStackBuilder(
    material,
    data
)

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
inline fun itemStack(
    material: Material, data: Int,
    value: ItemStackBuilder.() -> Unit
): ItemStackBuilder = itemStack(
    material,
    data
).apply(value)

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
            org.bukkit.block.BlockFace.UP
        ).type != Material.AIR
    )
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