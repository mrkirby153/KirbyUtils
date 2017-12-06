package me.mrkirby153.kcutils.structure

import me.mrkirby153.kcutils.getOrCreateSection
import me.mrkirby153.kcutils.structure.adapter.data.MaterialDataAdapter
import me.mrkirby153.kcutils.structure.adapter.state.BlockStateAdapter
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.configuration.file.YamlConfiguration

/**
 * A structure is a collection of blocks which can be placed anywhere
 *
 * @property yaml   The structure's data
 */
class Structure(private val yaml: YamlConfiguration) {

    /**
     * The size of the structure in the X direction from the origin
     */
    val sizeX: Int
        get() = yaml.getInt("dimensions.x")

    /**
     * The size of the structure in the Y direction from the origin
     */
    val sizeY: Int
        get() = yaml.getInt("dimensions.y")

    /**
     * The size of the structure in the Z direction from the origin
     */
    val sizeZ: Int
        get() = yaml.getInt("dimensions.z")

    /**
     * If the structure is placed
     */
    var placed = false

    /**
     * A list of blocks that make up the blocks that were replaced by the structure
     */
    private var originalBlocks = mutableListOf<BlockState>()

    /**
     * Place down the structure in the world
     *
     * @param origin    The location where the origin of the structure is placed
     */
    fun place(origin: Location) {
        if (placed)
            return
        // Get all the blocks
        val blockCount = yaml["blocks.count"] as Int
        for (i in 1..blockCount) {
            val section = yaml.getOrCreateSection("blocks.$i")

            val material = section.getString("material")

            val block = RelativeBlock(section.getInt("x"), section.getInt("y"), section.getInt("z"),
                    if (material != null) Material.valueOf(material) else null,
                    section.getInt("data").toByte())

            this.originalBlocks.add(block.getLocation(origin).block.state)

            block.place(origin, section.getConfigurationSection("state"))
        }
        placed = true
    }

    /**
     * Restores the surrounding area to its original state
     */
    fun restore() {
        if (!placed)
            return
        this.originalBlocks.forEach { state ->
            state.update(true, false)
        }
        placed = false
    }

    companion object {

        /**
         * Creates a structure
         *
         * @param origin    The origin of the structure (the base point)
         * @param pt1       The 1st point in the region encasing the structure
         * @param pt2       The 2nd point in the region encasing the structure
         * @param ignored   A list of ignored materials
         *
         * @return A configuration file of the structure
         */
        @JvmOverloads
        fun makeStructure(origin: Location, pt1: Location, pt2: Location, ignored: Array<Material> = arrayOf()): YamlConfiguration {
            val minX = Math.min(pt1.blockX, pt2.blockX)
            val minY = Math.min(pt1.blockY, pt1.blockY)
            val minZ = Math.min(pt1.blockZ, pt2.blockZ)

            val maxX = Math.max(pt1.blockX, pt2.blockX)
            val maxY = Math.max(pt1.blockY, pt2.blockY)
            val maxZ = Math.max(pt1.blockZ, pt2.blockZ)

            val yamlConfig = YamlConfiguration()

            val sizeX = maxX - minX + 1
            val sizeY = maxY - minY + 1
            val sizeZ = maxZ - minZ + 1

            var i = 1

            yamlConfig.getOrCreateSection("dimensions").apply {
                set("x", sizeX)
                set("y", sizeY)
                set("z", sizeZ)
            }

            for (x in minX..maxX) {
                for (y in minY..maxY) {
                    for (z in minZ..maxZ) {
                        val relX = x - origin.blockX
                        val relY = y - origin.blockY
                        val relZ = z - origin.blockZ

                        val loc = Location(origin.world, x.toDouble(), y.toDouble(), z.toDouble())
                        if(loc.block.type in ignored)
                            continue

                        yamlConfig.getOrCreateSection("blocks.${i++}").apply {
                            set("x", relX)
                            set("y", relY)
                            set("z", relZ)

                            val adapter = BlockStateAdapter.getAdapter(loc.block.state)
                            val dataAdapter = MaterialDataAdapter.getAdapter(loc.block.state.data)
                            set("material", loc.block.type.toString())
                            set("data", loc.block.data)

                            val stateSec = getOrCreateSection("state")
                            adapter?.serializeState(
                                    loc.block.state, stateSec)
                            dataAdapter?.serializeData(loc.block.state.data, stateSec)


                        }
                    }
                }
            }

            yamlConfig.set("blocks.count", i - 1)

            return yamlConfig
        }
    }
}