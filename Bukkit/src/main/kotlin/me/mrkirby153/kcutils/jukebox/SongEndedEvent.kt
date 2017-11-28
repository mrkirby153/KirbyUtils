package me.mrkirby153.kcutils.jukebox

import org.bukkit.event.Event
import org.bukkit.event.HandlerList

/**
 * Event fired when a [NoteBlockSong] ends
 */
class SongEndedEvent(val song: NoteBlockSong) : Event() {

    override fun getHandlers(): HandlerList {
        return handlerList
    }

    companion object {
       @JvmStatic val handlerList = HandlerList()
    }
}