package me.mrkirby153.kcutils.testplugin

import me.mrkirby153.kcutils.Chat
import me.mrkirby153.kcutils.Time
import org.bukkit.entity.Player
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
            true
        }
    }
}