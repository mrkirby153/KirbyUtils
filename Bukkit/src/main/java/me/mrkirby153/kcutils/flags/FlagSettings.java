package me.mrkirby153.kcutils.flags;

import java.util.HashMap;

/**
 * Stores the flags that are set for a world
 */
public class FlagSettings {

    private final String worldName;

    private final HashMap<WorldFlags, Boolean> setFlags = new HashMap<>();

    public FlagSettings(String worldName) {
        this.worldName = worldName;
    }

    /**
     * Gets a list of all the set flags in the world
     *
     * @return The flag list
     */
    public HashMap<WorldFlags, Boolean> getSetFlags() {
        return setFlags;
    }

    /**
     * Gets the world name
     *
     * @return The world name
     */
    public String getWorldName() {
        return worldName;
    }

    /**
     * Checks if a world flag is set
     *
     * @param flag The flag to check
     * @return True if the flag is set
     */
    public boolean isSet(WorldFlags flag) {
        if (setFlags.containsKey(flag))
            return setFlags.get(flag);
        else
            return flag.defaultValue();
    }

    /**
     * Sets a flag
     *
     * @param flag  The flag to set
     * @param state The state of the flag
     */
    public void setFlag(WorldFlags flag, boolean state) {
        this.setFlags.put(flag, state);
    }

    /**
     * Clears all the flags
     */
    void clear() {
        this.setFlags.clear();
    }
}
