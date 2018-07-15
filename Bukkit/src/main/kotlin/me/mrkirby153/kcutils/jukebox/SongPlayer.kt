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


    fun addPlayer(player: Player) {
        this.listeningPlayers.add(player.uniqueId)
    }

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

    open fun playChord(chord: List<Noteblock>?) {
        this.listeningPlayers.mapNotNull { Bukkit.getPlayer(it) }.forEach { player ->
            playChordAt(chord, player)
        }
    }

    @JvmOverloads
    fun playChordAt(chord: List<Noteblock>?, player: Player, location: Location = player.location) {
        if (chord == null) {
            return
        }
        chord.forEach { note ->
            val sound = when (note.instrument) {
                Noteblock.Instrument.PIANO -> Sound.BLOCK_NOTE_HARP
                Noteblock.Instrument.DOUBLE_BASS -> Sound.BLOCK_NOTE_BASS
                Noteblock.Instrument.BASS_DRUM -> Sound.BLOCK_NOTE_BASEDRUM
                Noteblock.Instrument.SNARE_DRUM -> Sound.BLOCK_NOTE_SNARE
                Noteblock.Instrument.CLICK -> Sound.BLOCK_NOTE_HAT
                Noteblock.Instrument.GUITAR -> Sound.BLOCK_NOTE_GUITAR
                Noteblock.Instrument.FLUTE -> Sound.BLOCK_NOTE_FLUTE
                Noteblock.Instrument.BELL -> Sound.BLOCK_NOTE_BELL
                Noteblock.Instrument.CHIME -> Sound.BLOCK_NOTE_CHIME
                Noteblock.Instrument.XYLOPHONE -> Sound.BLOCK_NOTE_XYLOPHONE
            }
            player.playSound(location, sound, SoundCategory.RECORDS, this.volume,
                    note.pitch.pitch)
        }
    }
}