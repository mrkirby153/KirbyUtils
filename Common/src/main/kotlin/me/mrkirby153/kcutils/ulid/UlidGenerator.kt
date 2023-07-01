package me.mrkirby153.kcutils.ulid

import java.security.SecureRandom
import java.util.Random
import kotlin.math.pow

private const val ENCODING = "0123456789ABCDEFGHJKMNPQRSTVWXYZ"
private const val ENCODING_LEN = ENCODING.length
private const val MAX_TIME = 0xffffffffffff
private const val TIME_LEN = 10L
private const val RANDOM_LEN = 16L


private fun encodeTime(time: Long, amount: Long): String {
    return buildString {
        var mod: Long
        var now = time
        for (len in amount - 1 downTo 0) {
            mod = now % ENCODING_LEN
            insert(0, ENCODING[mod.toInt()])
            now = (now - mod) / ENCODING_LEN
        }
    }
}

private fun encodeRandom(amount: Long, random: Random): String {
    return (0 until amount).map {
        val index = random.nextInt(ENCODING_LEN - 1)
        ENCODING[index]
    }.joinToString("")
}

private val defaultRandom = SecureRandom()

/**
 * Generates a new ulid. [time] can optionally be passed to generate a ulid from a fixed timestamp
 */
fun generateUlid(time: Long = System.currentTimeMillis(), random: Random = defaultRandom) =
    buildString {
        check(time < MAX_TIME) { "Time too large" }
        append(encodeTime(time, TIME_LEN))
        append(encodeRandom(RANDOM_LEN, random))
    }

/**
 * Converts a ulid to a timestamp
 */
fun ulidToTimestamp(id: String): Long {
    check(id.length.toLong() == TIME_LEN + RANDOM_LEN) { "Malformed ULID" }
    val time = id.substring(0, TIME_LEN.toInt())
        .reversed()
        .foldIndexed(0L) { index, carry, char ->
            val encodingIndex = ENCODING.indexOf(char)
            check(encodingIndex != -1) { "Invalid character: $char" }
            carry + encodingIndex * ENCODING_LEN.toDouble().pow(index.toDouble()).toLong()
        }
    check(time < MAX_TIME) { "Malformed ULID: Timestamp too large" }
    return time
}
