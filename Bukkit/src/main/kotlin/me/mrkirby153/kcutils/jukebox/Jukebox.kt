package me.mrkirby153.kcutils.jukebox

import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.plugin.java.JavaPlugin
import java.io.File
import java.util.*

/**
 * A collection of noteblock songs as a jukebox
 *
 * @param plugin    The owning plugin
 * @param players   A list of players listening to the jukebox
 */
class Jukebox(plugin: JavaPlugin, vararg players: Player) : Listener {

    /**
     * A list of players currently listening to the Jukebox
     */
    private val listeningPlayers = ArrayList<UUID>()

    /**
     * A list of files in the queue for the noteblocks
     */
    private val queue = LinkedList<File>()

    /**
     * The current song playing
     */
    private var nowPlaying: NoteBlockSong? = null

    /**
     * If the queue should repeat
     */
    private var repeat = false

    /**
     * The current position in the queue
     */
    private var queuePos = -1

    init {
        players.mapTo(listeningPlayers) { it.uniqueId }
        plugin.server.pluginManager.registerEvents(this, plugin)
    }

    /**
     * Adds a player to the jukebox
     *
     * @param player The player to add to the jukebox
     */
    fun addPlayer(player: Player) {
        if (!listeningPlayers.contains(player.uniqueId))
            listeningPlayers.add(player.uniqueId)
            nowPlaying?.addPlayer(player)
    }

    /**
     * Adds a song to the queue
     *
     * @param queuePosition The position in the queue
     * @param song          The song to add
     */
    fun addSong(queuePosition: Int, song: File) {
        queue.add(queuePosition - 1, song)
    }

    /**
     * Adds a song at the end of the queue
     *
     * @param song The song to add
     */
    fun addSong(song: File) {
        queue.add(song)
    }

    /**
     * Adds multiple songs to the queue
     *
     * @param startQueuePosition The position in the queue to start
     * @param songs              The songs to add
     */
    fun addSongs(startQueuePosition: Int, vararg songs: File) {
        var sqp = startQueuePosition
        for (s in songs) {
            addSong(sqp++, s)
        }
    }

    /**
     * Adds songs to the end of the queue
     *
     * @param songs The songs to add
     */
    fun addSongs(vararg songs: File) {
        for (s in songs) {
            addSong(s)
        }
    }

    /**
     * Gets the names of all the songs that are queued
     *
     * @return The song names of the queued songs
     */
    val queuedSongs: Array<String>
        get() {
            val songNames = mutableListOf<String>()
            songNames.addAll(queue.map { it.name})
            return songNames.toTypedArray()
        }

    /**
     * Skips to the next song in the Jukebox queue
     */
    fun nextSong() {
        if (nowPlaying == null)
            play()
        else
            nowPlaying!!.stop(true)
    }

    @EventHandler(ignoreCancelled = true)
    fun onSongEnded(event: SongEndedEvent) {
        if (event.song === nowPlaying) {
            nowPlaying = null
            queueNextSong()
            play()
        }
    }

    /**
     * Play the jukebox
     */
    fun play() {
        if (nowPlaying == null) {
            return
        }
        listeningPlayers.map { Bukkit.getPlayer(it) }.filter { it != null }.forEach { nowPlaying!!.addPlayer(it) }
        nowPlaying!!.play()
    }

    /**
     * Queue the next song in the jukebox
     */
    fun queueNextSong() {
        try {
            nowPlaying = NoteBlockSong(queue[queuePos++])
        } catch (e: IndexOutOfBoundsException) {
            queuePos = 0
            if (this.repeat) {
                queueNextSong()
            } else {
                nowPlaying = null
            }
        }

    }

    /**
     * Removes the player from the jukebox
     *
     * @param player The player to remove
     */
    fun removePlayer(player: Player) {
        listeningPlayers.remove(player.uniqueId)
        if (nowPlaying != null)
            nowPlaying!!.removePlayer(player)
    }

    /**
     * Sets the jukebox to repeat
     *
     * @param repeat Repeat or not
     */
    fun repeat(repeat: Boolean) {
        this.repeat = repeat
    }

    /**
     * Stops the jukebox
     */
    fun stop() {
        nowPlaying!!.stop(false)
        nowPlaying = null
        listeningPlayers.clear()
        queuePos = 0
    }
}
