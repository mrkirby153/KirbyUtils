package me.mrkirby153.kcutils.nms;

import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_11_R1.IChatBaseComponent;
import net.minecraft.server.v1_11_R1.Packet;
import net.minecraft.server.v1_11_R1.PacketPlayOutChat;
import net.minecraft.server.v1_11_R1.PacketPlayOutTitle;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

public class Compat1_11_R1 implements NMS {
    @Override
    public void actionBar(Player player, BaseComponent component) {
        PacketPlayOutChat chat = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + component.toLegacyText() + "\"}"), (byte) 2);
        sendPacket(player, chat);
    }

    @Override
    public void enable(JavaPlugin plugin) {

    }

    @Override
    public String getNMSVersion() {
        return "1.11_R1";
    }

    @Override
    public void title(Player player, String title, String subtitle) {
        sendPacket(player, constructTitle(title));
        sendPacket(player, constructSubtitle(subtitle));
    }

    @Override
    public void title(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        setTimings(player, fadeInTicks, stayTicks, fadeOutTicks);
        sendPacket(player, constructTitle(title));
        sendPacket(player, constructSubtitle(subtitle));
    }

    private PacketPlayOutTitle constructSubtitle(String subtitle) {
        return new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.SUBTITLE, IChatBaseComponent.ChatSerializer.a(String.format("{\"text\":\"%s\"}", (subtitle != null) ? subtitle : "")));
    }

    private PacketPlayOutTitle constructTitle(String title) {
        return new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TITLE, IChatBaseComponent.ChatSerializer.a(String.format("{\"text\":\"%s\"}", (title != null) ? title : "")));
    }

    private void sendPacket(Player player, Packet packet) {
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    private void setTimings(Player player, int fadeIn, int stay, int fadeOut) {
        PacketPlayOutTitle timingsPacket = new PacketPlayOutTitle(PacketPlayOutTitle.EnumTitleAction.TIMES, null, fadeIn, stay, fadeOut);
        sendPacket(player, timingsPacket);
    }

}
