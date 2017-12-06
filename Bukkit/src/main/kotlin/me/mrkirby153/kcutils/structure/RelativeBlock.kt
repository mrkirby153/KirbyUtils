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
class RelativeBlock(val x: Int, val y: Int, val z: Int, val material: Material?, val data: Byte?) {

    fun place(origin: Location, blockState: ConfigurationSection?) {
        val newLoc = getLocation(origin)
        // Mutate the stateo
        if (this.material != null)
            newLoc.block.type = this.material
        if (this.data != null)
            newLoc.block.data = this.data
        if (blockState != null) {
            val state = newLoc.block.state
            val materialAdapter = MaterialDataAdapter.getAdapter(state.data)
            materialAdapter?.let {
                state.data = materialAdapter.deserializeData(state.data, blockState)
            }
            BlockStateAdapter.getAdapter(newLoc.block.type)?.deserializeState(
                    state,
                    blockState)
            state.update(true, false)
        }
    }

    fun getLocation(origin: Location) = Location(origin.world, (this.x + origin.blockX).toDouble(),
            (this.y + origin.blockY).toDouble(), (this.z + origin.blockZ).toDouble())
}