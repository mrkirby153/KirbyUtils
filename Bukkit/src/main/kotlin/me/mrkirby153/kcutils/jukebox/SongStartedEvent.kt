package me.mrkirby153.kcutils.jukebox


import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.HandlerList

class SongStartedEvent(
        /**
         * The song that is playing
         *
         * @return The Song
         */
        val song: NoteBlockSong,
        /**
         * The player who started listening
         *
         * @return The player
         */
        val player: Player) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {

        val handlerList = HandlerList()
    }
}