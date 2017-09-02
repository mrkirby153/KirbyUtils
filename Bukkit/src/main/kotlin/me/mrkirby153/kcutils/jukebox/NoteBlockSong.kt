package me.mrkirby153.kcutils.jukebox

import com.google.common.io.ByteStreams
import org.bukkit.Bukkit
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException
import java.nio.file.Files
import java.util.*

class NoteBlockSong(private val location: File) {
    private var isDestroyed = false
    private val notes = HashMap<Int, ArrayList<NoteBlock>>()
    private var tempo: Double = 0.toDouble()
    private var currentTick = 0
    private var playerThread: Thread? = null
    private var isPlaying = true
    private val playersListening = ArrayList<String>()
    /**
     * Gets the song name (Loaded from the file)
     *
     * @return The song name
     */
    var songName: String? = null
        private set

    /**
     * "Tunes" the player to the song. Following notes will be played to the player
     *
     * @param player The player's name
     */
    fun addPlayer(player: String) {
        if (playersListening.contains(player))
            return
        playersListening.add(player)
        val p = Bukkit.getPlayerExact(player) ?: return
        Bukkit.getPluginManager().callEvent(SongStartedEvent(this, p))
    }

    /**
     * "Tunes" the player to the song. Following notes will be played to the player
     *
     * @param player The player object
     */
    fun addPlayer(player: Player) {
        addPlayer(player.name)
    }

    /**
     * Temporarily pauses the playback of the song
     */
    fun pause() {
        isPlaying = false
    }

    /**
     * Starts playback of the song
     */
    fun play() {
        loadSong()
        createThread()
    }

    /**
     * Untunes a player to the song
     *
     * @param player The player
     */
    fun removePlayer(player: Player) {
        playersListening.remove(player.name)
    }

    /**
     * Resumes playback of the song
     */
    fun resume() {
        isPlaying = true
    }

    /**
     * Stops the playback of the song and destroys it
     */
    fun stop(broadcastEvent: Boolean) {
        isPlaying = false
        currentTick = 0
        if (broadcastEvent)
            Bukkit.getPluginManager().callEvent(SongEndedEvent(this@NoteBlockSong))
        destroy()
    }

    /**
     * Creates the thread for playback
     */
    private fun createThread() {
        this.playerThread = Thread {
            while (!isDestroyed) {
                val startTime = System.currentTimeMillis()
                synchronized(this@NoteBlockSong) {
                    if (isPlaying) {
                        if (currentTick > notes.size) {
                            isPlaying = false
                            currentTick = 0
                            Bukkit.getPluginManager().callEvent(SongEndedEvent(this@NoteBlockSong))
                            destroy()
                            return@synchronized
                        }
                        val tick = currentTick++
                        for (s in playersListening) {
                            val p = Bukkit.getPlayerExact(s)
                            playTick(p, tick)
                        }
                    }
                    val duration = System.currentTimeMillis() - startTime
                    val delayMillis = delay * 50
                    if (duration < delayMillis) {
                        try {
                            Thread.sleep(delayMillis - duration)
                        } catch (e: InterruptedException) {
                            e.printStackTrace()
                        }

                    }
                }
            }
        }
        this.playerThread!!.name = "JukeboxThread"
        this.playerThread!!.priority = 10
        this.playerThread!!.start()
        playersListening.map { Bukkit.getPlayerExact(it) }.filter { it != null }.forEach { Bukkit.getPluginManager().callEvent(SongStartedEvent(this, it)) }
    }

    /**
     * Destroy the playback thread
     */
    private fun destroy() {
        isDestroyed = true
    }

    /**
     * Gets the delay for scheduling song playback
     *
     * @return The delay
     */
    private val delay: Short
        get() = (20 / tempo).toShort()

    /**
     * Parses the NoteBlockStudioParser file and loads them into [NoteBlockSong.notes] list
     */
    private fun loadSong() {
        try {
            val bytes = Files.readAllBytes(location.toPath())
            val input = ByteStreams.newDataInput(bytes)
            this.songName = input.readUTF()
            val totalChords = input.readInt()
            this.tempo = input.readDouble()
            for (i in 0 until totalChords) {
                val tick = input.readInt()
                val notesInChord = input.readInt()
                val chord = ArrayList<NoteBlock>()
                for (j in 0 until notesInChord) {
                    val instrument = input.readByte()
                    val pitch = input.readInt()
                    val nb = NoteBlock(tick, instrument, pitch)
                    chord.add(nb)
                }
                notes.put(tick, chord)
            }
        } catch (e: IOException) {
            e.printStackTrace()
        }

    }

    /**
     * Plays the corresponding tick to the player
     *
     * @param player The player to play the chord for
     * @param tick   The tick in the song to play
     */
    private fun playTick(player: Player?, tick: Int) {
        val chord = this.notes[tick]
        if (debug)
            println("TICK: " + tick)
        if (chord != null) {
            for (nb in chord) {
                var sound: Sound? = null
                when (nb.instrument) {
                    NoteBlock.Instrument.PIANO -> sound = Sound.BLOCK_NOTE_HARP
                    NoteBlock.Instrument.DOUBLE_BASS -> sound = Sound.BLOCK_NOTE_BASS
                    NoteBlock.Instrument.BASS_DRUM -> sound = Sound.BLOCK_NOTE_BASEDRUM
                    NoteBlock.Instrument.SNARE_DRUM -> sound = Sound.BLOCK_NOTE_SNARE
                    NoteBlock.Instrument.CLICK -> sound = Sound.BLOCK_NOTE_HAT
                }
                if (player == null)
                    return
                if (debug)
                    println("\tNOTE: " + nb.toString())
                player.playSound(player.location, sound, 0.75f, nb.pitch.noteblockPitch)
            }
        }
    }

    companion object {
        var debug = false
    }
}
