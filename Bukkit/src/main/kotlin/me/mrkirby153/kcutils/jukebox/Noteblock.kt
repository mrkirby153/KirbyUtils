package me.mrkirby153.kcutils.jukebox

class Noteblock(instrument: Byte, pitch: Byte) {

    /**
     * The instrument that this noteblock represents
     */
    val instrument = Instrument[instrument]

    /**
     * The pitch of the noteblock
     */
    val pitch = Pitch[pitch]


    /**
     * The instrument of the noteblocks
     */
    enum class Instrument(val raw: Byte) {
        PIANO(0),
        DOUBLE_BASS(1),
        BASS_DRUM(2),
        SNARE_DRUM(3),
        CLICK(4),
        GUITAR(5),
        FLUTE(6),
        BELL(7),
        CHIME(8),
        XYLOPHONE(9);

        companion object {

            /**
             * Parses a [Byte] into an [Instrument].
             *
             * @param raw The raw byte
             *
             * @return The instrument, or [PIANO] if an invalid byte is passed
             */
            operator fun get(raw: Byte): Instrument {
                values().forEach { v ->
                    if (v.raw == raw) {
                        return v
                    }
                }
                return PIANO
            }
        }
    }

    enum class Pitch(val raw: Byte, val pitch: Float) {
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

        override fun toString(): String {
            return "Pitch(${super.toString()}; pitch=${this.pitch})"
        }

        companion object {

            /**
             * Converts a raw pitch byte into a [Pitch]
             *
             * @param byte The raw byte
             * @return The [Pitch] or [F_SHARP_3] if an invalid byte was passed
             */
            operator fun get(byte: Byte): Pitch {
                values().forEach { v ->
                    if (v.raw == byte)
                        return v
                }
                return F_SHARP_3
            }
        }
    }

    override fun toString(): String {
        return "Noteblock(instrument=$instrument, pitch=$pitch)"
    }
}