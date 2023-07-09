package me.mrkirby153.kcutils.extensions

import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockFace

/**
 * Checks if this block is safe to teleport to
 */
fun Block.safeToTeleport(): Boolean {
    if (isLiquid || getRelative(BlockFace.DOWN).isLiquid)
        return false
    return !(type != Material.AIR || getRelative(BlockFace.UP).type != Material.AIR)
}