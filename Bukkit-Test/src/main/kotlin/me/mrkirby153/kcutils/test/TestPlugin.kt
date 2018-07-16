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

        val blockPosition = Reflections.getInstance("BlockPosition", location.blockX,
                location.blockY, location.blockZ)

        val packet = Reflections.getWrappedNMSClass("PacketPlayOutBlockBreakAnimation")
        packet.set(Integer::class.java, 0, id)
        packet.set("BlockPosition", 0, blockPosition)
        packet.set(Int::class.java, 1, 8)
        Reflections.sendPacket(player, packet)
    }
}