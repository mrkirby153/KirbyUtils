package me.mrkirby153.kcutils.testplugin

import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Time
import me.mrkirby153.kcutils.extensions.flags
import me.mrkirby153.kcutils.extensions.glowing
import me.mrkirby153.kcutils.extensions.itemStack
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Material
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {

    override fun onEnable() {
        val start = System.currentTimeMillis()
        logger.info("Hello, World!")
        val end = System.currentTimeMillis()
        logger.info("Initialized in ${Time.format(1, end - start)}")
        getCommand("test-command")?.setExecutor { sender, command, label, args ->
            if (sender !is Player) {
                sender.sendMessage(Chat.error("You must be a player to perform this command"))
                return@setExecutor true
            }
            sender.sendMessage(Chat.message("You are a player :D"))
                val sword = itemStack(Material.IRON_SWORD, 1) {
                    displayName(Component.text("Cool Sword").decoration(TextDecoration.ITALIC, false))
                    glowing = true
                    flags[ItemFlag.HIDE_UNBREAKABLE] = true
                    isUnbreakable = true
                }
            sender.inventory.addItem(sword)
            true
        }
    }
}