package me.mrkirby153.kcutils.flags;

import me.mrkirby153.kcutils.C;
import me.mrkirby153.kcutils.Module;
import me.mrkirby153.kcutils.command.CommandManager;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Sound;
import org.bukkit.SoundCategory;
import org.bukkit.World;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Arrays;
import java.util.HashMap;

public class FlagModule extends Module<JavaPlugin> {

    private HashMap<String, FlagSettings> worldSettings = new HashMap<>();

    public FlagModule(JavaPlugin plugin) {
        super("WorldFlags", "1.0", plugin);
    }

    public boolean get(World world, WorldFlags flag) {
        if (worldSettings.containsKey(world.getName())) {
            return worldSettings.get(world.getName()).isSet(flag);
        } else {
            return flag.defaultValue();
        }
    }

    public FlagSettings getFlags(World toActOn) {
        return worldSettings.get(toActOn.getName());
    }

    public FlagSettings initialize(World world) {
        FlagSettings settings = this.worldSettings.get(world.getName());
        if (settings == null)
            settings = new FlagSettings(world.getName());
        FlagSettings finalSettings = settings;
        Arrays.stream(WorldFlags.values()).forEach(flag -> finalSettings.setFlag(flag, flag.defaultValue()));
        this.worldSettings.put(world.getName(), settings);
        return settings;
    }

    public void set(World world, WorldFlags flag, boolean state, boolean announce) {
        if (!worldSettings.containsKey(world.getName()))
            return;
        worldSettings.get(world.getName()).setFlag(flag, state);
        if (announce)
            world.getPlayers().forEach(player -> {
                TextComponent component = C.formattedChat(flag.getFriendlyName() + "> ", C.TAG_COLOR, C.Style.BOLD);

                TextComponent s = C.formattedChat(state ? "true" : "false", state ? ChatColor.GREEN : ChatColor.RED, C.Style.BOLD);
                component.addExtra(s);
                player.spigot().sendMessage(component);
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_PLING, SoundCategory.MASTER, 1F, 1F);
            });
    }

    public void set(World world, WorldFlags flag, boolean state) {
        set(world, flag, state, true);
    }

    public boolean shouldCancel(World world, WorldFlags flag) {
        return !get(world, flag);
    }

    @Override
    protected void init() {
        registerListener(new FlagListener(this));
        if (CommandManager.instance() != null)
            CommandManager.instance().registerCommand(new FlagCommand(getPlugin(), this));
        else
            log("[WARN] Command manager not initialized! "+FlagCommand.class+" needs to be registered manually!");
    }
}
