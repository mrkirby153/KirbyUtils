package me.mrkirby153.kcutils.structure.adapter.data

import org.bukkit.block.BlockFace
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.material.Directional

class DirectionalData : DataAdapter<Directional>() {
    override fun deserialize(materialData: Directional, config: ConfigurationSection) {
        materialData.setFacingDirection(BlockFace.valueOf(config.getString("facing", "NORTH")))
    }

    override fun serialize(materialData: Directional, config: ConfigurationSection) {
        config.set("facing", materialData.facing.toString())
    }

}