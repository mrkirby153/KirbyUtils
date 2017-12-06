package me.mrkirby153.kcutils.structure.adapter.state

import org.bukkit.block.Chest
import org.bukkit.block.Sign
import org.bukkit.configuration.ConfigurationSection

class SignAdapter : BlockAdapter<Sign>() {

    override fun deserialize(state: Sign, config: ConfigurationSection) {
        for (i in state.lines.indices) {
            val toString = config["line.$i"].toString()
            state.setLine(i, toString)
        }
    }

    override fun serialize(state: Sign, config: ConfigurationSection) {
        for (i in state.lines.indices) {
            config["line.$i"] = state.getLine(i)
        }
    }
}

class ChestAdapter : BlockAdapter<Chest>() {
    override fun deserialize(state: Chest, config: ConfigurationSection) {
        for (i in 0 until state.blockInventory.size) {
            state.blockInventory.setItem(i, config.getItemStack("items.$i"))
        }
    }

    override fun serialize(state: Chest, config: ConfigurationSection) {
        for (i in 0 until state.blockInventory.size) {
            config.set("items.$i", state.blockInventory.getItem(i))
        }
    }
}