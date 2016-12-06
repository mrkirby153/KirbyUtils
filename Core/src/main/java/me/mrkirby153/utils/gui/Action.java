package me.mrkirby153.utils.gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;

public interface Action {

    void onClick(Player player, ClickType clickType);
}
