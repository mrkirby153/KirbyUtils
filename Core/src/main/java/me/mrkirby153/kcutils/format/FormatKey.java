package me.mrkirby153.kcutils.format;

/**
 * A class mapping a key to be formatted to a value.
 * The actual format of the key depends on the implementation of the formatting algorithm
 */
public class FormatKey {

    private final String key;
    private final Object value;

    public FormatKey(String key, Object value) {
        this.key = key;
        this.value = value;
    }

    /**
     * Gets the key to be formatted
     *
     * @return The key to be replaced
     */
    public String getKey() {
        return key;
    }

    /**
     * Gets the value to be formatted
     *
     * @return The value
     */
    public Object getValue() {
        return value;
    }
}
