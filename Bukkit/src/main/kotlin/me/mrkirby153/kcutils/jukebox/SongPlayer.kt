package me.mrkirby153.kcutils.jukebox

import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.SoundCategory
import org.bukkit.entity.Player
import org.bukkit.plugin.java.JavaPlugin
import java.util.UUID
import java.util.function.Consumer

open class SongPlayer(plugin: JavaPlugin) : Runnable {

    private val listeningPlayers = mutableListOf<UUID>()

    var song: Song? = null
        set(song) {
            field = song
            // Reset the song when the song is changed
            this.tick = 0
        }

    var playing = false

    var volume = 1.0F

    var tick = 0

    var repeat = false

    private val playingThread: Thread = Thread(this).apply {
        name = "SongPlayer-${plugin.name}"
        isDaemon = true
        priority = Thread.MAX_PRIORITY
    }

    var destroyed = false

    private val callbacks = mutableListOf<Consumer<Song>>()

    init {
        playingThread.start()
    }


    /**
     * Tunes a player into the jukebox. They will hear the song from the jukebox.
     *
     * @param player The player to tune
     */
    fun addPlayer(player: Player) {
        this.listeningPlayers.add(player.uniqueId)
    }

    /**
     * Untunes a player from the jukebox. They will not longer hear the song from the jukebox.
     *
     * @param player The player to remove
     */
    fun removePlayer(player: Player) {
        this.listeningPlayers.remove(player.uniqueId)
    }

    /**
     * Adds a callback to be run when the song ends
     *
     * @param consumer The callback
     */
    fun addCallback(consumer: Consumer<Song>) {
        this.callbacks.add(consumer)
    }

    override fun run() {
        var startTime: Long
        synchronized(this) {
            while (!destroyed) {
                startTime = System.currentTimeMillis() // The start time of the loop
                if (song == null) {
                    Thread.sleep(1)
                    continue
                }
                val song = this.song!!
                if (playing) {
                    if (this.tick > song.length) {
                        if (repeat) {
                            this.tick = 0
                            Thread.sleep(1)
                            continue
                        }
                        this.playing = false
                        this.song = null
                        this.tick = 0
                        this.callbacks.forEach { c ->
                            c.accept(song)
                        }
                        continue
                    }
                    // Play the tick
                    val chord = song.getTick(this.tick)
                    playChord(chord)
                    val loopDuration = System.currentTimeMillis() - startTime // The duration of the loop
                    val delayMillis = (20 / song.tempo) * 50
                    if (loopDuration < delayMillis) {
                        Thread.sleep((delayMillis - loopDuration).toLong())
                    }
                    this.tick++
                }
                if (System.currentTimeMillis() - startTime <= 0) {
                    // If the loop takes less tha 1ms, it doesn't work
                    Thread.sleep(1)
                }
            }
        }
    }

    /**
     * Plays a chord
     *
     * @param chord A list of [Noteblocks][Noteblock] to play to all listning players
     */
    open fun playChord(chord: List<Noteblock>?) {
        this.listeningPlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
            playChordAt(chord, player)
        }
    }

    /**
     * Plays a chord at a specific location
     *
     * @param chord The chord to play
     * @param player The player to play to
     * @param location The location to play the chord at (Useful for static jukeboxes)
     */
    @JvmOverloads
    fun playChordAt(chord: List<Noteblock>?, player: Player, location: Location = player.location) {
        if (chord == null) {
            return
        }
        chord.forEach { note ->
            val sound = when (note.instrument) {
                Noteblock.Instrument.PIANO -> Sound.BLOCK_NOTE_BLOCK_HARP
                Noteblock.Instrument.DOUBLE_BASS -> Sound.BLOCK_NOTE_BLOCK_BASS
                Noteblock.Instrument.BASS_DRUM -> Sound.BLOCK_NOTE_BLOCK_BASEDRUM
                Noteblock.Instrument.SNARE_DRUM -> Sound.BLOCK_NOTE_BLOCK_SNARE
                Noteblock.Instrument.CLICK -> Sound.BLOCK_NOTE_BLOCK_HAT
                Noteblock.Instrument.GUITAR -> Sound.BLOCK_NOTE_BLOCK_GUITAR
                Noteblock.Instrument.FLUTE -> Sound.BLOCK_NOTE_BLOCK_FLUTE
                Noteblock.Instrument.BELL -> Sound.BLOCK_NOTE_BLOCK_BELL
                Noteblock.Instrument.CHIME -> Sound.BLOCK_NOTE_BLOCK_CHIME
                Noteblock.Instrument.XYLOPHONE -> Sound.BLOCK_NOTE_BLOCK_XYLOPHONE
            }
            player.playSound(location, sound, SoundCategory.RECORDS, this.volume,
                    note.pitch.pitch)
        }
    }
}