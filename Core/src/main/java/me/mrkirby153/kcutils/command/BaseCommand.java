package me.mrkirby153.kcutils.command;

import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseCommand<T extends JavaPlugin> {

    private String name;

    private String[] aliases;

    private String permission;

    private T instance;

    private String aliasUsed;

    public BaseCommand(T instance, String name, String permission, String... aliases) {
        this.name = name;
        this.instance = instance;
        this.permission = permission;
        this.aliases = aliases;
    }

    public BaseCommand(T instance, String name, String... aliases) {
        this(instance, name, null, aliases);
    }

    public abstract void execute(Player player, String[] args);

    public String[] getAliases() {
        return aliases;
    }

    public String getName() {
        return this.name;
    }

    public String getPermission() {
        return permission;
    }

    public boolean isAlias(String command) {
        for (String s : aliases) {
            if (s.equalsIgnoreCase(command))
                return true;
        }
        return false;
    }

    public void setAlasUsed(String alias) {
        this.aliasUsed = alias;
    }

    public List<String> tabComplete(Player player, String command, String[] args) {
        return new ArrayList<>();
    }

    protected String getAliasUsed() {
        return aliasUsed;
    }

    protected T getInstance() {
        return instance;
    }
}
