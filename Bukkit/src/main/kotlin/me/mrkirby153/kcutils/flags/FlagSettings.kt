package me.mrkirby153.kcutils.flags

import java.util.*

/**
 * Stores the flags that are set for a world
 */
class FlagSettings(
        /**
         * Gets the world name
         *
         * @return The world name
         */
        val worldName: String) {

    /**
     * Gets a list of all the set flags in the world
     *
     * @return The flag list
     */
    val setFlags = HashMap<WorldFlags, Boolean>()

    /**
     * Checks if a world flag is set
     *
     * @param flag The flag to check
     * @return True if the flag is set
     */
    fun isSet(flag: WorldFlags): Boolean {
        return setFlags[flag] ?: flag.defaultValue()
    }

    /**
     * Sets a flag
     *
     * @param flag  The flag to set
     * @param state The state of the flag
     */
    fun setFlag(flag: WorldFlags, state: Boolean) {
        this.setFlags.put(flag, state)
    }

    /**
     * Clears all the flags
     */
    internal fun clear() {
        this.setFlags.clear()
    }
}
