package me.mrkirby153.kcutils.structure

import com.sk89q.worldedit.MaxChangedBlocksException
import com.sk89q.worldedit.Vector
import com.sk89q.worldedit.bukkit.BukkitWorld
import com.sk89q.worldedit.bukkit.WorldEditPlugin
import com.sk89q.worldedit.data.DataException
import com.sk89q.worldedit.schematic.MCEditSchematicFormat
import com.sk89q.worldedit.world.World
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.io.IOException

/**
 * A class for handling structure classes
 */
class Structure(private val schematicFile: File) {

    /**
     * If the schematic is placed in the world
     */
    var isPlaced = false
        private set

    /**
     * The location that the schematic is placed
     */
    var placedAt: Location? = null
        private set

    /**
     * A worldedit instance
     */
    private val worldEdit: WorldEditPlugin? = Bukkit.getPluginManager().getPlugin("WorldEdit") as? WorldEditPlugin

    private var wePresent = worldEdit != null

    /**
     * Place the structure
     *
     * @param block The location to place the structure at
     */
    fun place(block: Location) {
        if (!wePresent)
            throw IllegalStateException("WorldEdit is required to load structures!")
        val session = worldEdit!!.worldEdit.editSessionFactory.getEditSession(BukkitWorld(block.world) as World, Integer.MAX_VALUE)
        try {
            val clipboard = MCEditSchematicFormat.getFormat(schematicFile).load(schematicFile)
            val origin = Vector(block.blockX, block.blockY, block.blockZ)
            clipboard.paste(session, origin, false)
        } catch (e: MaxChangedBlocksException) {
            e.printStackTrace()
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: DataException) {
            e.printStackTrace()
        }

        this.placedAt = block.clone()
        this.isPlaced = true
    }

    /**
     * Resets the structure
     */
    fun reset() {
        this.placedAt = null
        this.isPlaced = false
    }
}
