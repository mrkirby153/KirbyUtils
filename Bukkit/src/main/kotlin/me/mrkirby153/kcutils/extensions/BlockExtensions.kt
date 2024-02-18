package me.mrkirby153.kcutils.extensions

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockState
import org.bukkit.block.data.BlockData

/**
 * Checks if this block is safe to teleport to
 */
fun Block.safeToTeleport(): Boolean {
    if (isLiquid || getRelative(BlockFace.DOWN).isLiquid)
        return false
    return !(type != Material.AIR || getRelative(BlockFace.UP).type != Material.AIR)
}

/**
 * Queries information about a [Block]'s state
 */
inline fun <reified T : BlockState, U> Block.state(toApply: (T) -> U): U {
    check(state is T) { "Cannot set block state of type ${T::class.java} as it is ${state::class.java}" }
    return toApply(state as T)
}

/**
 * Queries information about a [Block]'s [BlockData]
 */
inline fun <reified T : BlockData, U> Block.data(mapper: (T) -> U): U? {
    check(this.blockData is T) {
        return null
    }
    return mapper(this.blockData as T)
}

/**
 * Updates a [Block]'s [BlockData] with the provided [toApply]
 */
inline fun <reified T : BlockData> Block.updateBlockData(
    physics: Boolean = false,
    toApply: (T) -> Unit
) {
    check(this.blockData is T) {
        "Cannot set block data of type ${T::class.java} as it is ${this.blockData::class.java}"
    }
    this.setBlockData((this.blockData as T).apply(toApply), physics)
}