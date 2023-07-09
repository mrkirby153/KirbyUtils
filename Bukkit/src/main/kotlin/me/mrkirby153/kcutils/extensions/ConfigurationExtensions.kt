package me.mrkirby153.kcutils.extensions

import org.bukkit.configuration.ConfigurationSection

/**
 * Gets the given [section]. If it does not exist, it will be created
 */
fun ConfigurationSection.getOrCreateSection(section: String) =
    getConfigurationSection(section) ?: createSection(section)