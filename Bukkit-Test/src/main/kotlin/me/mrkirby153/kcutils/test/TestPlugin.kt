package me.mrkirby153.kcutils.test

import me.mrkirby153.kcutils.reflections.Reflections
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.plugin.java.JavaPlugin
import java.util.Random

class TestPlugin : JavaPlugin(), Listener {

    override fun onEnable() {
        logger.info(Reflections.getNMSClass("BiomeBase").canonicalName)
        server.pluginManager.registerEvents(this, this)
    }


    @EventHandler
    fun interact(event: PlayerInteractEvent) {
        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            breakBlock(event.player, event.clickedBlock.location)
            event.player.playSound(event.player.location, Sound.BLOCK_NOTE_CHIME, 1F, 1F)
        }
    }

    fun breakBlock(player: Player, location: Location) {
        val id = Random().nextInt()
        val packet = Reflections.getNMSClass("PacketPlayOutBlockBreakAnimation")

        val constructor = packet.getConstructor(Int::class.java, Reflections.getNMSClass("BlockPosition"), Int::class.java)


        val instance = constructor.newInstance(id, getBlockPosition(location), 3)

        Reflections.sendPacket(player, instance)
    }

    fun getBlockPosition(location: Location): Any {
        val blockPosClass = Reflections.getNMSClass("BlockPosition")
        val constructor = blockPosClass.getConstructor(Int::class.java, Int::class.java,
                Int::class.java)
        return constructor.newInstance(location.blockX, location.blockY, location.blockZ)
    }
}