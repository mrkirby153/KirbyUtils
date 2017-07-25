package me.mrkirby153.kcutils.flags;

public enum WorldFlags {

    // PvP - Player vs Player Damage
    PVP_ENABLE(true, "PvP"),

    // PvE - Environmental damage
    PVE_ENABLE(true, "PvE"),

    // Blocks can be broken
    BLOCK_BREAK(true, "Block Break"),

    // Blocks can be placed
    BLOCK_PLACE(true, "Block Place"),

    // Blocks will burn
    BLOCK_BURN(true, "Block Burn"),

    // Blocks will yield experience
    BLOCK_DROP_EXP(true, "XP Drop"),

    EXPLOSIONS(true, "Explosions"),

    // Blocks can be pushed
    BLOCK_PUSH(true, "Piston Push"),

    // Blocks can be interacted with
    BLOCK_INTERACT(true, "Block Interact"),

    // Blocks will respond to redstone updates
    BLOCK_REDSTONE(true, "Redstone"),

    // Blocks will dispense items
    BLOCK_DISPENSE(true, "Dispensing"),

    // Blocks will grow
    BLOCK_GROW(true, "Growth"),

    // Hostile mobs will spawn
    HOSTILE_SPAWN(true, "Hostile Mobs"),

    // Peaceful mobs will spawn
    PEACEFUL_SPAWN(true, "Peaceful Mobs"),

    // Sheep regrow wool
    SHEEP_REGROW_WOLL(true, "Wool Regrowth"),

    // Buckets can be used
    BUCKET_USE(true, "Buckets"),

    // Sheep can be sheared
    SHEEP_SHEARING(true, "Shearing"),

    // Fall damage will be dealt
    FALL_DAMAGE(true, "Fall Damage"),

    // Weather will change
    WEATHER_CHANGE(true, "Weather"),

    // Structures such as trees/mushrooms will be allowed to grow
    STRUCTURE_GROWTH(true, "Growth"),

    // Leaves will decay
    LEAF_DECAY(true, "Leaf Decay"),

    // Health will regenerate
    HEALTH_REGEN(true, "Health Regeneration"),

    // Hunger will deplete
    HUNGER_DEPLETE(true, "Hunger Depletion");

    private final boolean defaultEnabled;

    private final String friendlyName;

    WorldFlags(boolean defaultEnabled, String friendlyName) {
        this.defaultEnabled = defaultEnabled;
        this.friendlyName = friendlyName;
    }

    public boolean defaultValue() {
        return defaultEnabled;
    }

    public String getFriendlyName() {
        return friendlyName;
    }
}
