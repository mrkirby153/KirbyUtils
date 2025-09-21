package me.mrkirby153.kcutils.extensions

import org.bukkit.NamespacedKey
import org.bukkit.plugin.java.JavaPlugin


fun JavaPlugin.namespacedKey(value: String): NamespacedKey = NamespacedKey(this, value)