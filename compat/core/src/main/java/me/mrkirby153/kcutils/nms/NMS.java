package me.mrkirby153.kcutils.nms;

import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public interface NMS {

    void enable(JavaPlugin plugin);

    String getNMSVersion();

    void title(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks);

    void title(Player player, String title, String subtitle);

    void actionBar(Player player, BaseComponent component);

    void removeOceans();
}
