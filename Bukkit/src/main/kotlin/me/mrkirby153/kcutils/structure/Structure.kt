package me.mrkirby153.kcutils.structure

import me.mrkirby153.kcutils.extensions.getOrCreateSection
import me.mrkirby153.kcutils.structure.adapter.data.MaterialDataAdapter
import me.mrkirby153.kcutils.structure.adapter.state.BlockStateAdapter
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.BlockState
import org.bukkit.block.Container
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.plugin.java.JavaPlugin

/**
 * A structure is a collection of blocks which can be placed anywhere
 *
 * @property yaml   The structure's data
 */
class Structure(private val yaml: YamlConfiguration) : Runnable {

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

    var placePhase = 0

    var subPhase = 1
    var maxPhase = 0
    var taskId = 0
    var origin: Location? = null

    val blocks: Array<RelativeBlock> = loadBlocks()

    /**
     * Place down the structure in the world all at once
     *
     * @param origin    The location where the origin of the structure is placed
     */
    fun placeAll(origin: Location) {
        if (placed)
            return

        this.blocks.forEach {
            this.originalBlocks.add(it.getLocation(origin).block.state)
            it.place(origin)
        }
        placed = true
    }

    /**
     * Place down the structure in phases in attempt to prevent as many glitches as possible
     *
     * @param origin    The origin
     * @param plugin    The plugin to schedule the task under
     * @oaram period    The delay in ticks between phases
     */
    @JvmOverloads
    fun place(origin: Location, plugin: JavaPlugin, period: Long = 2L) {
        if (placed)
            return
        this.blocks.forEach { block ->
            if (block.blockState != null) {
                if (this.maxPhase < block.blockState.getInt("placeOrder")) {
                    this.maxPhase = block.blockState.getInt("placeOrder")
                }
            }
        }
        this.origin = origin
        taskId = plugin.server.scheduler.scheduleSyncRepeatingTask(plugin, this, 0L, period)
    }

    override fun run() {
        if (origin == null || placePhase > maxPhase) {
            Bukkit.getServer().scheduler.cancelTask(taskId)
            if (origin != null)
                placed = true
        }
        val blocks = this.blocks.filter {
            (it.blockState?.getInt("placeOrder") ?: 0) == this.placePhase
        }
        blocks.forEach { block ->
            if (subPhase == 1)
                this.originalBlocks.add(block.getLocation(origin!!).block.state)

            block.place(origin!!, subPhase)
        }
        subPhase++
        if (subPhase > 3) {
            subPhase = 1
            placePhase++
        }
    }

    fun loadBlocks(): Array<RelativeBlock> {
        val count = yaml["blocks.count"] as Int

        val mutableList = mutableListOf<RelativeBlock>()

        for (i in 1..count) {
            val section = yaml.getConfigurationSection("blocks.$i") ?: continue

            val material = section.getString("material")!!.uppercase()
            if (!Material.values().map { it.toString() }.contains(material))
                continue

            val block = RelativeBlock(section.getInt("x"), section.getInt("y"), section.getInt("z"),
                    Material.valueOf(material), section.getConfigurationSection("state"))

            mutableList.add(block)
        }
        return mutableList.toTypedArray()
    }

    /**
     * Restores the surrounding area to its original state
     */
    fun restore() {
        if (!placed)
            return
        this.originalBlocks.forEach { state ->
            val currentState = state.location.block.state
            // Clear the inventory of the container before we remove it to prevent popping items on the ground
            if (currentState is Container) {
                currentState.inventory.clear()
            }
            state.update(true, false)
        }
        this.originalBlocks.clear()
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
        fun makeStructure(origin: Location, pt1: Location, pt2: Location,
                          ignored: Array<Material> = arrayOf()): YamlConfiguration {
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
                        if (loc.block.type in ignored)
                            continue

                        yamlConfig.getOrCreateSection("blocks.${i++}").apply {
                            set("x", relX)
                            set("y", relY)
                            set("z", relZ)

                            val adapter = BlockStateAdapter.getAdapter(loc.block.state)
                            val dataAdapter = MaterialDataAdapter.getAdapter(loc.block.state.data)
                            set("material", loc.block.type.toString())

                            val stateSec = getOrCreateSection("state")
                            adapter?.serializeState(
                                    loc.block.state, stateSec)
                            if (adapter != null)
                                stateSec.set("placeOrder", adapter.placeOrder)
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