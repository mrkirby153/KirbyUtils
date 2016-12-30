package me.mrkirby153.kcutils.nms;

import com.google.common.base.Throwables;
import net.md_5.bungee.api.chat.BaseComponent;
import net.minecraft.server.v1_11_R1.*;
import org.bukkit.craftbukkit.v1_11_R1.entity.CraftPlayer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class Compat1_11_R1 implements NMS {
    @Override
    public void actionBar(Player player, BaseComponent component) {
        PacketPlayOutChat chat = new PacketPlayOutChat(IChatBaseComponent.ChatSerializer.a("{\"text\":\"" + component.toLegacyText() + "\"}"), (byte) 2);
        sendPacket(player, chat);
    }

    @Override
    public void removeOceans() {
        try {
            // Oceans with plains
            setStaticFinal(Biomes.class.getField("a"), Biomes.c);
            setStaticFinal(Biomes.class.getField("b"), Biomes.c);

            setStaticFinal(Biomes.class.getField("z"), Biomes.c);

            // Frozen oceans with ice flats
            setStaticFinal(Biomes.class.getField("l"), Biomes.n);
        } catch (Exception e) {
            Throwables.propagate(e);
        }
    }

    private void setStaticFinal(Field field, Object value) throws IllegalAccessException, NoSuchFieldException {
        field.setAccessible(true);
        Field mf = Field.class.getDeclaredField("modifiers");
        mf.setAccessible(true);
        mf.setInt(field, field.getModifiers() & ~Modifier.FINAL);
        field.set(null, value);
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
