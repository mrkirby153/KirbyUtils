package me.mrkirby153.kcutils.command;

import me.mrkirby153.kcutils.C;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChatTabCompleteEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * A command manager for commands
 */
public class CommandManager implements Listener {

    private static CommandManager instance;

    private List<BaseCommand> registeredCommands = new ArrayList<>();

    private HashMap<String, BaseCommand<? extends JavaPlugin>> commands = new HashMap<>();


    private CommandManager(JavaPlugin plugin) {
        plugin.getServer().getPluginManager().registerEvents(this, plugin);
    }

    /**
     * Initializes the manager (Registers events)
     *
     * @param plugin The plugin to initialize for
     */
    public static void initialize(JavaPlugin plugin) {
        if (instance == null) {
            instance = new CommandManager(plugin);
        }
    }

    /**
     * Gets the Command Manager instance
     *
     * @return The command manager
     */
    public static CommandManager instance() {
        return instance;
    }

    @EventHandler
    public void commandPreProcess(PlayerCommandPreprocessEvent event) {
        if (!event.getMessage().startsWith("/"))
            return;
        String commandName = event.getMessage().substring(1);
        String[] args = null;
        if (commandName.contains(" ")) {
            commandName = commandName.split(" ")[0];
            args = event.getMessage().substring(event.getMessage().indexOf(' ') + 1).split(" ");
        }
        if (args == null) {
            args = new String[0];
        }
        BaseCommand<?> command = findCommand(commandName);
        if (command != null) {
            event.setCancelled(true);
            if (!event.getPlayer().hasPermission(command.getPermission())) {
                if (!event.getPlayer().isOp()) {
                    event.getPlayer().spigot().sendMessage(C.e("You do not have permission to perform this command! You are missing the permission " + command.getPermission()));
                    return;
                }
            }
            command.setAlasUsed(commandName);
            command.execute(event.getPlayer(), args);
        }
    }

    /**
     * Registers a command
     *
     * @param command The {@link BaseCommand Command} to register
     */
    public void registerCommand(BaseCommand<? extends JavaPlugin> command) {
        this.commands.put(command.getName().toLowerCase(), command);
    }

    @EventHandler
    public void tabComplete(PlayerChatTabCompleteEvent event) {
        String commandName = event.getChatMessage();
        String[] args = null;
        if (commandName.contains(" ")) {
            commandName = commandName.split(" ")[0];
            args = event.getChatMessage().substring(event.getChatMessage().indexOf(' ') + 1).split(" ");
        }
        if (args == null)
            args = new String[0];
        BaseCommand<?> command = findCommand(commandName);
        if (command != null) {
            List<String> suggestions = command.tabComplete(event.getPlayer(), commandName, args);
            event.getTabCompletions().clear();
            event.getTabCompletions().addAll(suggestions);
        }
    }

    /**
     * Finds a command by its name or alias
     *
     * @param command The command
     * @return The {@link BaseCommand Command} or null if one wasn't found
     */
    private BaseCommand<? extends JavaPlugin> findCommand(String command) {
        if (this.commands.containsKey(command.toLowerCase()))
            return this.commands.get(command);
        // Find the command by its alias
        for (BaseCommand<? extends JavaPlugin> c : commands.values()) {
            if (c.isAlias(command))
                return c;
        }
        return null;
    }
}
