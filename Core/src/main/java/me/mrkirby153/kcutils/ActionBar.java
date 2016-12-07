package me.mrkirby153.kcutils;

import me.mrkirby153.kcutils.nms.NMS;
import me.mrkirby153.kcutils.nms.NMSFactory;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class ActionBar extends Module<JavaPlugin> implements Listener, Runnable {

    private NMS nms;
    private HashMap<UUID, BaseComponent> actionBars = new HashMap<>();


    public ActionBar(JavaPlugin plugin) {
        super("actionbar", "1.0", plugin);
        nms = new NMSFactory(plugin).getNMS();
    }

    @Override
    public void run() {
        List<UUID> toDelete = new ArrayList<>();
        actionBars.forEach((u, s) -> {
            Player player = Bukkit.getPlayer(u);
            if(player == null){
                toDelete.add(u);
                return;
            }
            nms.actionBar(player, s);
        });
        actionBars.entrySet().removeIf(entry -> toDelete.contains(entry.getKey()));
    }

    @Override
    protected void init() {
        registerListener(this);
        getPlugin().getServer().getScheduler().scheduleSyncRepeatingTask(getPlugin(), this, 0L, 5L);
    }

    public void set(Player player, BaseComponent text){
        actionBars.put(player.getUniqueId(), text);
    }

    public void clear(Player player){
        actionBars.remove(player.getUniqueId());
        nms.actionBar(player, new TextComponent(""));
    }
}
