package me.mrkirby153.kcutils;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

/**
 * Handle conversions from human-readable times and computer readbale times
 */
public class Time {

    public static final String DATE_FORMAT_NOW = "MM-dd-yy HH:mm:ss";
    public static final String DATE_FORMAT_DAY = "MM-dd-yy";

    /**
     * Convert milliseconds to the specified time unit
     *
     * @param trim The amount of decimal places
     * @param time The time
     * @param type The time unit tot convert to
     * @return The converted time
     */
    public static double convert(int trim, long time, TimeUnit type) {
        if (type == TimeUnit.FIT) {
            if (time < 60000) type = TimeUnit.SECONDS;
            else if (time < 3600000) type = TimeUnit.MINUTES;
            else if (time < 86400000) type = TimeUnit.HOURS;
            else type = TimeUnit.DAYS;
        }

        if (type == TimeUnit.DAYS) return trim(trim, time / 86400000d);
        if (type == TimeUnit.HOURS) return trim(trim, time / 3600000d);
        if (type == TimeUnit.MINUTES) return trim(trim, time / 60000d);
        if (type == TimeUnit.SECONDS) return trim(trim, time / 1000d);
        else return time;
    }

    /**
     * Gets a String representing the current date in the format: <pre>MM-dd-yy</pre>
     *
     * @return The date
     */
    public static String date() {
        return new SimpleDateFormat(DATE_FORMAT_DAY).format(Calendar.getInstance().getTime());
    }

    /**
     * Formats milliseconds into human readable format
     *
     * @param trim The amount of decimal places
     * @param time The time
     * @param type The time unit to display in
     * @return A string in human-readable format
     */
    public static String format(int trim, long time, TimeUnit type) {
        if (time == -1) return "Permanent";

        if (type == TimeUnit.FIT) {
            if (time < 60000) type = TimeUnit.SECONDS;
            else if (time < 3600000) type = TimeUnit.MINUTES;
            else if (time < 86400000) type = TimeUnit.HOURS;
            else type = TimeUnit.DAYS;
        }

        String text;
        if (type == TimeUnit.DAYS) text = trim(trim, time / 86400000d) + " Days";
        else if (type == TimeUnit.HOURS) text = trim(trim, time / 3600000d) + " Hours";
        else if (type == TimeUnit.MINUTES) text = trim(trim, time / 60000d) + " Minutes";
        else if (type == TimeUnit.SECONDS) text = trim(trim, time / 1000d) + " Seconds";
        else text = trim(0, time) + " Milliseconds";

        return text;
    }

    /**
     * Gets a String representing the current time in the format: <pre>MM-dd-yy HH:mm:ss</pre>
     *
     * @return The date
     */
    public static String now() {
        return new SimpleDateFormat(DATE_FORMAT_NOW).format(Calendar.getInstance().getTime());
    }

    /**
     * Trims the double to the specified number of decimal places
     *
     * @param degree The quantity of decimal places
     * @param d      The double to trim
     * @return A trimmed double
     */
    public static double trim(int degree, double d) {
        if (degree == 0) {
            return Math.round(d);
        }
        String format = "#.#";
        for (int i = 1; i < degree; i++) {
            format += "#";
        }
        DecimalFormatSymbols symb = new DecimalFormatSymbols(Locale.US);
        DecimalFormat twoDForm = new DecimalFormat(format, symb);
        return Double.valueOf(twoDForm.format(d));
    }

    public enum TimeUnit {
        FIT,
        DAYS,
        HOURS,
        MINUTES,
        SECONDS
    }

}
