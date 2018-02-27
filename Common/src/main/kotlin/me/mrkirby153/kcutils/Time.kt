package me.mrkirby153.kcutils

import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

/**
 * Handle conversions from human-readable times and computer readable times
 */
object Time {

    val DATE_FORMAT_NOW = "MM-dd-yy HH:mm:ss"
    val DATE_FORMAT_DAY = "MM-dd-yy"

    private val timeMap = mutableMapOf<String, Int>()

    init {
        timeMap.clear()
        timeMap.put("ms", 1)
        timeMap.put("s", 1000)
        timeMap.put("m", 60000)
        timeMap.put("h", 3600000)
        timeMap.put("d", 86400000)

        timeMap.put("milliseconds", 1)
        timeMap.put("seconds", 1000)
        timeMap.put("minutes", 60000)
        timeMap.put("hours", 3600000)
        timeMap.put("days", 86400000)
    }

    /**
     * Convert milliseconds to the specified time unit
     *
     * @param trim The amount of decimal places
     * @param time The time
     * @param type The time unit tot convert to
     * @return The converted time
     */
    fun convert(trim: Int, time: Long, type: TimeUnit): Double {
        val t = if (type == TimeUnit.FIT) fitTime(time, TimeUnit.SECONDS) else type
        return trim(trim, time / t.ms.toDouble())
    }

    /**
     * Gets a String representing the current date in the format: <pre>MM-dd-yy</pre>
     *
     * @return The date
     */
    fun date(): String {
        return SimpleDateFormat(DATE_FORMAT_DAY).format(Calendar.getInstance().time)
    }

    /**
     * Formats milliseconds into human readable format
     *
     * @param trim The amount of decimal places
     * @param time The time
     * @param type The time unit to display in
     * @param smallest The smallest time unit to display
     * @return A string in human-readable format
     */
    @JvmOverloads
    fun format(trim: Int, time: Long, type: TimeUnit = TimeUnit.FIT,
               smallest: TimeUnit = TimeUnit.MILLISECONDS): String {
        var type = type
        if (time == -1L) return "Permanent"

        if (type == TimeUnit.FIT) {
            type = fitTime(time, smallest)
        }

        return buildString {
            val t = time / type.ms.toDouble()
            val t1 = if (trim == 0)
                Math.round(t).toInt().toString()
            else
                trim(trim, t).toString()
            append(t1)
            append(" ")
            append(if(t1 == "1") type.singleName else type.pluralName)
        }
    }

    /**
     * Calculates the largest time unit of the given time
     */
    private fun fitTime(time: Long, smallest: TimeUnit): TimeUnit {
        var determined: TimeUnit = smallest
        val values = TimeUnit.values().drop(1)
        if (time > values.first().ms) {
            determined = values.first()
        } else {
            for (i in 0 until values.size) {
                if (time < values[Math.max(i - 1, 0)].ms) {
                    determined = values[i]
                }
            }
        }

        return if (determined.smaller(smallest)) {
            smallest
        } else {
            determined
        }
    }

    /**
     * Gets a String representing the current time in the format: <pre>MM-dd-yy HH:mm:ss</pre>
     *
     * @return The date
     */
    fun now(): String {
        return SimpleDateFormat(DATE_FORMAT_NOW).format(Calendar.getInstance().time)
    }

    /**
     * Trims the double to the specified number of decimal places
     *
     * @param degree The quantity of decimal places
     * @param d      The double to trim
     * @return A trimmed double
     */
    fun trim(degree: Int, d: Double): Double {
        if (degree == 0) {
            return Math.round(d).toDouble()
        }
        var format = "#.#"
        for (i in 1 until degree) {
            format += "#"
        }
        val symb = DecimalFormatSymbols(Locale.US)
        val twoDForm = DecimalFormat(format, symb)
        return java.lang.Double.valueOf(twoDForm.format(d))
    }

    /**
     * Converts a string from a human-readable format (i.e "30 seconds" or "30s") into milliseconds
     *
     * @param time The time string
     * @return The time in milliseconds
     */
    fun parse(time: String): Long {
        val timePattern = Pattern.compile("(\\d+\\s?)(\\D+)")
        val timeMatcher = timePattern.matcher(time)

        var offset = 0L
        while (timeMatcher.find()) {
            val t = timeMatcher.group(1).trim().toLong()
            val multiplier = timeMatcher.group(2).trim()
            if (!this.timeMap.containsKey(multiplier)) {
                throw IllegalArgumentException("Time format \"$multiplier\" not found")
            }
            offset += t * (this.timeMap[multiplier] ?: 0)
        }
        return offset
    }

    enum class TimeUnit(val ms: Long, val pluralName: String, val singleName: String) {
        FIT(-1, "FIT", "FIT"),
        DAYS(86400000, "Days", "Day"),
        HOURS(3600000, "Hours", "Hour"),
        MINUTES(60000, "Minutes", "Minute"),
        SECONDS(1000, "Seconds", "Second"),
        MILLISECONDS(1, "Milliseconds", "Millisecond");

        fun smaller(other: TimeUnit) = other.ordinal <= this.ordinal

        fun greater(other: TimeUnit) = !smaller(other)
    }

}
