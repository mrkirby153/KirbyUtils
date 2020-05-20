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

    private const val DATE_FORMAT_NOW = "MM-dd-yy HH:mm:ss"
    private const val DATE_FORMAT_DAY = "MM-dd-yy"

    private val timeMap = mutableMapOf<String, Long>()

    init {
        timeMap.clear()
        timeMap["ms"] = TimeUnit.MILLISECONDS.ms
        timeMap["s"] = TimeUnit.SECONDS.ms
        timeMap["m"] = TimeUnit.MINUTES.ms
        timeMap["h"] = TimeUnit.HOURS.ms
        timeMap["d"] = TimeUnit.DAYS.ms
        timeMap["w"] = TimeUnit.WEEKS.ms
        timeMap["y"] = TimeUnit.YEARS.ms

        timeMap["milliseconds"] = TimeUnit.MILLISECONDS.ms
        timeMap["seconds"] = TimeUnit.SECONDS.ms
        timeMap["minutes"] = TimeUnit.MINUTES.ms
        timeMap["hours"] = TimeUnit.HOURS.ms
        timeMap["days"] = TimeUnit.DAYS.ms
        timeMap["weeks"] = TimeUnit.WEEKS.ms
        timeMap["years"] = TimeUnit.YEARS.ms
    }

    /**
     * Convert milliseconds to the specified time unit
     *
     * @param trim The amount of decimal places
     * @param time The time
     * @param type The time unit tot convert to
     * @return The converted time
     */
    @JvmStatic
    fun convert(trim: Int, time: Long, type: TimeUnit): Double {
        val t = if (type == TimeUnit.FIT) fitTime(time, TimeUnit.SECONDS) else type
        return trim(trim, time / t.ms.toDouble())
    }

    /**
     * Gets a String representing the current date in the format: <pre>MM-dd-yy</pre>
     *
     * @return The date
     */
    @JvmStatic
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
     *
     * @return A string in human-readable format
     */
    @JvmOverloads
    @JvmStatic
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
            append(if (t1 == "1") type.singleName else type.pluralName)
        }
    }

    /**
     * Formats milliseconds into human readable format
     *
     * @param time The time
     * @param smallest The smallest unit to display
     *
     * @return A string in human-readable format
     */
    @JvmOverloads
    @JvmStatic
    fun formatLong(time: Long, smallest: TimeUnit = TimeUnit.SECONDS,
                   short: Boolean = false): String {
        val string = buildString {
            var mutableTime = time
            val timeUnits = TimeUnit.values().drop(1)
            val map = mutableMapOf<TimeUnit, Long>()
            timeUnits.forEach {
                if (it.ordinal > smallest.ordinal)
                    return@forEach
                val count = mutableTime / it.ms
                if (count > 0) {
                    map[it] = count
                    mutableTime -= it.ms * count
                }
            }

            map.entries.forEachIndexed { index, entry ->
                val unit = entry.key
                val t = entry.value
                append(t)
                if (short)
                    append(unit.shortName)
                else {
                    append(" ")
                    if (t == 1L) {
                        append(unit.singleName)
                    } else {
                        append(unit.pluralName)
                    }
                }
                if (!short) {
                    if (index + 1 == map.entries.size - 1) {
                        if (map.entries.size > 2)
                            append(",")
                        append(" and ")
                    } else {
                        if (index + 1 < map.entries.size)
                            append(", ")
                    }
                }
            }
        }
        return string
    }

    /**
     * Calculates the largest time unit of the given time
     */
    @JvmStatic
    private fun fitTime(time: Long, smallest: TimeUnit): TimeUnit {
        var determined: TimeUnit = smallest
        val values = TimeUnit.values().drop(1)
        if (time >= values.first().ms) {
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
    @JvmStatic
    fun now(): String {
        return SimpleDateFormat(DATE_FORMAT_NOW).format(Calendar.getInstance().time)
    }

    /**
     * Trims the double to the specified number of decimal places
     *
     * @param degree The quantity of decimal places
     * @param d      The double to trim
     *
     * @return A trimmed double
     */
    @JvmStatic
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
     *
     * @return The time in milliseconds
     */
    @JvmStatic
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

    /**
     * An enum representing a time unit
     */
    enum class TimeUnit(val ms: Long, val pluralName: String, val singleName: String,
                        val shortName: String) {
        FIT(-1, "FIT", "FIT", "FIT"),
        YEARS(31536000000, "Years", "Year", "y"),
        WEEKS(604800000, "Weeks", "Week", "w"),
        DAYS(86400000, "Days", "Day", "d"),
        HOURS(3600000, "Hours", "Hour", "h"),
        MINUTES(60000, "Minutes", "Minute", "m"),
        SECONDS(1000, "Seconds", "Second", "s"),
        MILLISECONDS(1, "Milliseconds", "Millisecond", "ms");

        fun smaller(other: TimeUnit) = other.ordinal <= this.ordinal

        fun greater(other: TimeUnit) = !smaller(other)
    }

}
