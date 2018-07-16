package me.mrkirby153.kcutils.test

import org.bukkit.plugin.java.JavaPlugin

class TestPlugin : JavaPlugin() {

    override fun onEnable() {
        logger.info("Hello World!")
    }
}