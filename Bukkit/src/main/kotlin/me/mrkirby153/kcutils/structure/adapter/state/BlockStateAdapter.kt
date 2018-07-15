package me.mrkirby153.kcutils.structure.adapter.state

import org.bukkit.block.BlockState
import org.bukkit.block.Chest
import org.bukkit.block.Sign

/**
 * A mapper for mapping block states to their corresponding adapter
 */
object BlockStateAdapter {


    val adapters = mutableMapOf<Class<out BlockState>, BlockAdapter<*>>()


    init {
        adapters[Sign::class.java] = SignAdapter()
        adapters[Chest::class.java] = ChestAdapter()
    }

    /**
     * Gets an adapter by its [BlockState]
     *
     * @param state The state
     *
     * @return The adapter, or null if it doesn't exist
     */
    @JvmStatic
    fun getAdapter(state: BlockState): BlockAdapter<*>? {
        var foundAdapter: BlockAdapter<*>? = null
        adapters.forEach { clazz, adapter ->
            if (clazz.isAssignableFrom(state.javaClass))
                foundAdapter = adapter
        }
        return foundAdapter
    }

    /**
     * Registers a [BlockStateAdapter]
     *
     * @param state The [BlockState] to register the adapater for
     * @param adapter The adapter to register
     */
    @JvmStatic
    fun registerAdapter(state: Class<out BlockState>, adapter: BlockAdapter<*>) {
        this.adapters[state] = adapter
    }
}