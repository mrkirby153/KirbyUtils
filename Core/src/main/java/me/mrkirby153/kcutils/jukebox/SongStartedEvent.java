package me.mrkirby153.kcutils.jukebox;


import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

public class SongStartedEvent extends Event {

    private static final HandlerList handlers = new HandlerList();

    private NoteBlockSong song;
    private Player player;

    public SongStartedEvent(NoteBlockSong song, Player player) {
        this.song = song;
        this.player = player;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    /**
     * The player who started listening
     *
     * @return The player
     */
    public Player getPlayer() {
        return player;
    }

    /**
     * The song that is playing
     *
     * @return The Song
     */
    public NoteBlockSong getSong() {
        return song;
    }
}