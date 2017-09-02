package me.mrkirby153.kcutils.jukebox

/**
 * Class to handle deconstructing of noteblocks from binary into a playable form
 *
 * @author mrkirby153
 */
class NoteBlock(val tick: Int, inst: Byte, key: Int) {

    /**
     * Gets the [Instrument] that the [NoteBlock] represents
     *
     * @return The [Instrument] that the [NoteBlock] represents
     */
    val instrument: Instrument
    /**
     * Gets the pitch that the [NoteBlock] represents
     *
     * @return the [Pitch]
     */
    val pitch: Pitch

    init {
        this.instrument = Instrument.getInstrument(inst)
        this.pitch = Pitch.findPitch(key.toByte())
    }

    /**
     * Custom toString() override containing the instrument, pitch, and the tick the
     *
     * @return A custom string in the following format: [Instrument] string, [Pitch] string and the tick
     */
    override fun toString(): String {
        return instrument.toString() + ", " + pitch.toString() + ", " + tick
    }

    /**
     * Enum for turning binary data (0-4) into a more user-friendly representation
     */
    enum class Instrument(
            /**
             * Returns the raw byte that the instrument represents
             *
             * @return The instrument's byte (0-4)
             */
            val inst: Int) {
        INVALID(-1),
        PIANO(0),
        DOUBLE_BASS(1),
        BASS_DRUM(2),
        SNARE_DRUM(3),
        CLICK(4);


        companion object {

            /**
             * Converts a byte of an instrument into its more friendly notation
             *
             * @param inst The instrument (0-4)
             * @return The [Instrument] corresponding to the byte
             */
            fun getInstrument(inst: Byte): Instrument {
                when (inst) {
                    0.toByte() -> return PIANO
                    1.toByte() -> return DOUBLE_BASS
                    2.toByte() -> return BASS_DRUM
                    3.toByte() -> return SNARE_DRUM
                    4.toByte() -> return CLICK
                }
                return INVALID
            }
        }
    }

    /**
     * Enum handling a note's binary representation as well as its playSound() pitch
     */
    enum class Pitch constructor(
            /**
             * Gets the raw pitch (33-57) that the [Pitch] represents
             *
             * @return Raw pitch data
             */
            val pitch: Int,
            /**
             * Gets the noteblock pitch for using in playSound()
             *
             * @return The current noteblock pitch
             */
            val noteblockPitch: Float) {
        ERR(-1, -1f),
        F_SHARP_3(33, 0.5f),
        G_3(34, .53f),
        G_SHARP_3(35, 0.56f),
        A_3(36, 0.600f),
        A_SHARP_3(37, 0.630f),
        B_3(38, 0.670f),
        C_4(39, 0.700f),
        C_SHARP_4(40, 0.760f),
        D_4(41, 0.800f),
        D_SHARP_4(42, 0.84f),
        E_4(43, 0.900f),
        F_4(44, 0.940f),
        F_SHARP_4(45, 1.000f),
        G_4(46, 1.060f),
        G_SHARP_4(47, 1.120f),
        A_4(48, 1.180f),
        A_SHARP_4(49, 1.260f),
        B_4(50, 1.340f),
        C_5(51, 1.42f),
        C_SHARP_5(52, 1.500f),
        D_5(53, 1.600f),
        D_SHARP_5(54, 1.680f),
        E_5(55, 1.780f),
        F_5(56, 1.88f),
        F_SHARP_5(57, 2.000f);

        /**
         * Modified toString() to include the noteblock's pitch
         *
         * @return A modified version of the [Enum&#39;s][Enum] toString(), appending the noteblock pitch
         */
        override fun toString(): String {
            val s = super.toString()
            return s + ":" + noteblockPitch
        }

        companion object {

            /**
             * Converts the raw pitch byte into a [Pitch]
             *
             * @param pitch The pitch byte (33-57)
             * @return A [Pitch]
             */
            fun findPitch(pitch: Byte): Pitch {
                val p = pitch.toInt()
                val pitches = Pitch::class.java.enumConstants
                pitches
                        .filter { it.pitch == p }
                        .forEach { return it }
                return ERR
            }

            /**
             * Converts the noteblock pitch into a [Pitch]
             *
             * @param pitch The noteblock pitch
             * @return A [Pitch]
             */
            fun findPitch(pitch: Float): Pitch {
                val pitches = Pitch::class.java.enumConstants
                pitches
                        .filter { it.noteblockPitch == pitch }
                        .forEach { return it }
                return ERR
            }
        }
    }
}