package me.mrkirby153.kcutils.jukebox;

import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

/**
 * Event fired when a {@link NoteBlockSong} ends
 */
public class SongEndedEvent extends Event {
    private static final HandlerList handlers = new HandlerList();
    private NoteBlockSong song;

    public SongEndedEvent(NoteBlockSong song) {
        this.song = song;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public NoteBlockSong getSong() {
        return song;
    }
}