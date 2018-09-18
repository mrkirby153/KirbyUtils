package me.mrkirby153.kcutils.jukebox

import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder

class Song(val file: File) {

    /**
     * A list of notes in the file
     */
    private val notes: MutableMap<Int, MutableList<Noteblock>> = mutableMapOf()

    init {
        parse()
    }

    /**
     * The tempo of the song
     */
    var tempo: Double = 0.0

    /**
     * The name of the song
     */
    var name = "Unknown"

    var length: Short = 0

    private var buffer: ByteBuffer = ByteBuffer.allocate(0)

    /**
     * Parses the song
     */
    private fun parse() {
        val bytes = file.readBytes()
        buffer = ByteBuffer.wrap(bytes)
        buffer.clear()
        buffer.order(ByteOrder.LITTLE_ENDIAN)

        parseHeader()
        parseNotes()
    }

    /**
     * Parses the header
     */
    private fun parseHeader() {
        this.length = buffer.short
        val height = buffer.short
        this.name = buffer.string
        val author = buffer.string
        val origAuthor = buffer.string
        val desc = buffer.string
        this.tempo = buffer.short / 100.0


        val autoSave = buffer.get()
        val asDuration = buffer.get()
        val timeSignature = buffer.get()
        val minutesSpent = buffer.int
        val lClicks = buffer.int
        val rClicks = buffer.int
        val added = buffer.int
        val removed = buffer.int

        val schemaName = buffer.string
    }

    /**
     * Parse all the notes in the song
     */
    private fun parseNotes() {
        var tick = 0
        while (true) {
            val jumps = buffer.short
            if (jumps == 0.toShort())
                break
            tick += jumps
            var layer = -1
            while (true) {
                val layerJumps = buffer.short
                if (layerJumps == 0.toShort())
                    break
                layer += layerJumps
                val noteblock = Noteblock(buffer.get(), buffer.get())

                var list = this.notes[tick]
                if (list == null) {
                    list = mutableListOf()
                    this.notes[tick] = list
                }
                list.add(noteblock)
            }
        }
    }

    /**
     * Dumps all the notes to the console for easy debugging
     */
    fun dumpNotes() {
        this.notes.forEach { tick, notes ->
            println(buildString {
                appendln("Tick $tick")
                notes.forEach { note ->
                    appendln("\t - $note")
                }
            })
        }
    }

    fun getTick(int: Int) = this.notes[int]

    private val ByteBuffer.string: String
        get() {
            val size = this.int
            val data = ByteArray(size)
            this.get(data)
            return String(data)
        }
}