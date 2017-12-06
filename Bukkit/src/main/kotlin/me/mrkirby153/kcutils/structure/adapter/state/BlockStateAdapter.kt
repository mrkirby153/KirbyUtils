package me.mrkirby153.kcutils.structure.adapter.state

import org.bukkit.Material
import org.bukkit.block.BlockState

/**
 * A mapper for mapping block states to their corresponding adapter
 */
object BlockStateAdapter {


    val adapters = mutableMapOf<Material, BlockAdapter<*>>()


    init {
        adapters.put(Material.SIGN, SignAdapter())
        adapters.put(Material.SIGN_POST, SignAdapter())
        adapters.put(Material.WALL_SIGN, SignAdapter())
        adapters.put(Material.CHEST, ChestAdapter())
    }

    /**
     * Gets an adapter by its [BlockState]
     *
     * @param state The state
     *
     * @return The adapter, or null if it doesn't exist
     */
    fun getAdapter(state: BlockState) = adapters[state.type]

    /**
     * Gets an adapter by its [Material]
     *
     * @param material The material of the block
     *
     * @return The adapter, or null if it doesn't exist
     */
    fun getAdapter(material: Material) = adapters[material]
}