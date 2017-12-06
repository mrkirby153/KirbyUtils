package me.mrkirby153.kcutils.structure

import me.mrkirby153.kcutils.structure.adapter.data.MaterialDataAdapter
import me.mrkirby153.kcutils.structure.adapter.state.BlockStateAdapter
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.configuration.ConfigurationSection

/**
 * A block that is relative to a certain point
 *
 * @property x          The amount in the X-direction the block is
 * @property y          The amount in the Y-Direction the block is
 * @property z          The amount in the Z-direction the block is
 * @property material   The material the block is made of
 * @property data       The data value of the material
 */
class RelativeBlock(val x: Int, val y: Int, val z: Int, val material: Material,
                    val blockState: ConfigurationSection?) {

    @JvmOverloads
    fun place(origin: Location, phase: Int = 0) {
        val newLoc = getLocation(origin)

        val state = newLoc.block.state
        when (phase) {
            0 -> {
                // Trigger everything at once
                place(origin, 1)
                place(origin, 2)
                place(origin, 3)
            }
            1 -> {
                // Set the material & data
                newLoc.block.type = this.material
            }
            2 -> {
                // Set the block state
                if (blockState != null) {
                    BlockStateAdapter.getAdapter(state)?.deserializeState(state, blockState)
                    state.update(true, false)
                }
            }
            3 -> {
                // Set the block's material data
                if (blockState != null) {
                    MaterialDataAdapter.getAdapter(state.data)?.let {
                        state.data = it.deserializeData(state.data,
                                blockState)
                    }
                    state.update(true, false)
                }
            }
        }
    }

    fun getLocation(origin: Location) = Location(origin.world, (this.x + origin.blockX).toDouble(),
            (this.y + origin.blockY).toDouble(), (this.z + origin.blockZ).toDouble())
}