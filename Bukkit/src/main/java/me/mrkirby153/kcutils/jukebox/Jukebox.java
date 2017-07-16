package me.mrkirby153.kcutils.jukebox;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.*;

/**
 * A collection of noteblock songs as a jukebox
 */
public class Jukebox implements Listener {

    private List<UUID> listeningPlayers = new ArrayList<>();

    private LinkedList<File> queue = new LinkedList<>();

    private NoteBlockSong nowPlaying;

    private boolean repeat = false;

    private int queuePos = -1;

    private boolean ended = false;

    public Jukebox(JavaPlugin plugin, Player... players) {
        if (players != null)
            for (Player p : players) {
                listeningPlayers.add(p.getUniqueId());
            }
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Adds a player to the jukebox
     *
     * @param player The player to add to the jukebox
     */
    public void addPlayer(Player player) {
        if (!listeningPlayers.contains(player.getUniqueId()))
            listeningPlayers.add(player.getUniqueId());
        if (nowPlaying != null)
            nowPlaying.addPlayer(player);
    }

    /**
     * Adds a song to the queue
     *
     * @param queuePosition The position in the queue
     * @param song          The song to add
     */
    public void addSong(int queuePosition, File song) {
        queue.add(queuePosition - 1, song);
    }

    /**
     * Adds a song at the end of the queue
     *
     * @param song The song to add
     */
    public void addSong(File song) {
        queue.add(song);
    }

    /**
     * Adds multiple songs to the queue
     *
     * @param startQueuePosition The position in the queue to start
     * @param songs              The songs to add
     */
    public void addSongs(int startQueuePosition, File... songs) {
        for (File s : songs) {
            addSong(startQueuePosition++, s);
        }
    }

    /**
     * Adds songs to the end of the queue
     *
     * @param songs The songs to add
     */
    public void addSongs(File... songs) {
        for (File s : songs) {
            addSong(s);
        }
    }

    /**
     * Gets the names of all the songs that are queued
     *
     * @return The song names of the queued songs
     */
    public String[] getQueuedSongs() {
        String[] songNames = new String[queue.size()];
        for (int i = 0; i < songNames.length; i++) {
            songNames[i] = queue.get(i).getName();
        }
        return songNames;
    }

    /**
     * Skips to the next song in the Jukebox queue
     */
    public void nextSong() {
        if (nowPlaying == null)
            play();
        else
            nowPlaying.stop(true);
    }

    @EventHandler(ignoreCancelled = true)
    public void onSongEnded(SongEndedEvent event) {
        if (event.getSong() == nowPlaying) {
            nowPlaying = null;
            queueNextSong();
            play();
        }
    }

    /**
     * Play the jukebox
     */
    public void play() {
        if (nowPlaying == null) {
            return;
        }
        listeningPlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).forEach(nowPlaying::addPlayer);
        nowPlaying.play();
    }

    /**
     * Queue the next song in the jukebox
     */
    public void queueNextSong() {
        try {
            nowPlaying = new NoteBlockSong(queue.get(queuePos++));
        } catch (IndexOutOfBoundsException e) {
            queuePos = 0;
            if (this.repeat) {
                queueNextSong();
            } else {
                nowPlaying = null;
            }
        }
    }

    /**
     * Removes the player from the jukebox
     *
     * @param player The player to remove
     */
    public void removePlayer(Player player) {
        listeningPlayers.remove(player.getUniqueId());
        if (nowPlaying != null)
            nowPlaying.removePlayer(player);
    }

    /**
     * Sets the jukebox to repeat
     *
     * @param repeat Repeat or not
     */
    public void repeat(boolean repeat) {
        this.repeat = repeat;
    }

    /**
     * Stops the jukebox
     */
    public void stop() {
        nowPlaying.stop(false);
        nowPlaying = null;
        listeningPlayers.clear();
        queuePos = 0;
    }
}
