package me.mrkirby153.kcutils.flags;

import me.mrkirby153.kcutils.C;
import me.mrkirby153.kcutils.command.BaseCommand;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class FlagCommand extends BaseCommand<JavaPlugin> {

    private FlagModule module;

    public FlagCommand(JavaPlugin instance, FlagModule module) {
        super(instance, "ku-flags", "kirbyutils.command.flag", new String[]{});
        this.module = module;
    }

    @Override
    public void execute(Player player, String[] args) {
        World toActOn = player.getWorld();

        if (args.length == 0) {
            // Build a list of flags and their state
            StringBuilder flags = new StringBuilder();
            flags.append(ChatColor.BLUE).append(toActOn.getName()).append(" Flags> ");

            FlagSettings settings = module.getFlags(toActOn);
            settings.getSetFlags().forEach((flag, state) -> {
                flags.append(state ? ChatColor.GREEN : ChatColor.RED);
                flags.append(flag.toString());
            });
            player.sendMessage(flags.toString());
        }
        if (args.length == 2) {
            String flag = args[0];
            boolean state = Boolean.parseBoolean(args[1]);
            WorldFlags f;
            try {
                f = WorldFlags.valueOf(flag.toUpperCase());
            } catch (IllegalArgumentException e) {
                player.spigot().sendMessage(C.e("That is not a valid flag!"));
                return;
            }
            module.set(toActOn, f, state);
        }
    }
}
