package me.mrkirby153.kcutils.structure.adapter.state

import org.bukkit.block.BlockState
import org.bukkit.configuration.ConfigurationSection

/**
 * An adapter for converting a block's state into a serializable format
 */
abstract class BlockAdapter<in S : BlockState> {

    abstract fun deserialize(state: S, config: ConfigurationSection)

    abstract fun serialize(state: S, config: ConfigurationSection)

    /**
     * Deserializes the state of the block
     *
     * @param state     The state of the block
     * @param config    The configuration of the state
     */
    fun deserializeState(state: BlockState, config: ConfigurationSection) {
        this.deserialize(state as S, config)
    }

    /**
     * Serializes the state of the block
     * @param state     The current state of the block
     * @param config    The configuration section to write to
     */
    fun serializeState(state: BlockState, config: ConfigurationSection) {
        this.serialize(state as S, config)
    }
}