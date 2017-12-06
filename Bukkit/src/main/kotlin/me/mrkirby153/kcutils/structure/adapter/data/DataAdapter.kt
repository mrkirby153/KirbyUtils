package me.mrkirby153.kcutils.structure.adapter.data

import org.bukkit.configuration.ConfigurationSection
import org.bukkit.material.MaterialData

/**
 * An adapter for converting a block's data into a serializable format
 */
abstract class DataAdapter<in M> {

    abstract fun deserialize(materialData: M, config: ConfigurationSection)

    abstract fun serialize(materialData: M, config: ConfigurationSection)


    /**
     * Deserializes the data
     *
     * @param data      The material data
     * @param config    The configuration
     *
     * @return The deserialized material data
     */
    fun deserializeData(data: MaterialData, config: ConfigurationSection): MaterialData {
        val d = data.clone()
        this.deserialize(d as M, config)
        return d
    }

    /**
     * Serialzies the data
     *
     * @param data      The material data
     * @param config    The configuration
     */
    fun serializeData(data: MaterialData, config: ConfigurationSection) {
        this.serialize(data as M, config)
    }
}