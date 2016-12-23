package me.mrkirby153.kcutils.nms.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Throwables;
import me.mrkirby153.kcutils.nms.NMS;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class CompatProtocolLib implements NMS {

    private ProtocolManager protocolManager;

    @Override
    public void actionBar(Player player, BaseComponent component) {
        PacketContainer actionBar = protocolManager.createPacket(PacketType.Play.Server.CHAT);
        actionBar.getBytes().write(0, (byte) 2);
        actionBar.getChatComponents().write(0, WrappedChatComponent.fromText(component.toLegacyText()));
        sendPacket(player, actionBar);
    }

    @Override
    public void enable(JavaPlugin plugin) {
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }

    @Override
    public String getNMSVersion() {
        return "ProtocolLib";
    }

    @Override
    public void title(Player player, String title, String subtitle) {
        sendPackets(player, constructTitle(title, subtitle, 20, 20, 20));
    }

    @Override
    public void title(Player player, String title, String subtitle, int fadeInTicks, int stayTicks, int fadeOutTicks) {
        sendPackets(player, constructTitle(title, subtitle, fadeInTicks, stayTicks, fadeOutTicks));
    }

    /**
     * Constructs a series of packets to send a title to a player
     *
     * @param title    The title
     * @param subtitle The subtitle
     * @param fadeIn   The fade in ticks
     * @param stay     The ticks the title should stay
     * @param fadeOut  The fade out ticks
     * @return A list of packets to send
     */
    private List<PacketContainer> constructTitle(String title, String subtitle, int fadeIn, int stay, int fadeOut) {
        List<PacketContainer> packets = new ArrayList<>();

        PacketContainer timings = protocolManager.createPacket(PacketType.Play.Server.TITLE);
        timings.getTitleActions().write(0, EnumWrappers.TitleAction.TIMES);
        if (fadeIn != -1)
            timings.getIntegers().write(0, fadeIn);
        if (stay != -1)
            timings.getIntegers().write(1, stay);
        if (fadeOut != -1)
            timings.getIntegers().write(2, fadeOut);
        packets.add(timings);

        PacketContainer titlePacket = protocolManager.createPacket(PacketType.Play.Server.TITLE);
        titlePacket.getModifier().writeDefaults();

        titlePacket.getTitleActions().write(0, EnumWrappers.TitleAction.TITLE);
        titlePacket.getChatComponents().write(0, WrappedChatComponent.fromText(title));

        packets.add(titlePacket);

        if (subtitle != null) {
            PacketContainer subtitlePacket = protocolManager.createPacket(PacketType.Play.Server.TITLE);
            subtitlePacket.getModifier().writeDefaults();

            subtitlePacket.getTitleActions().write(0, EnumWrappers.TitleAction.SUBTITLE);
            subtitlePacket.getChatComponents().write(0, WrappedChatComponent.fromText(subtitle));

            packets.add(subtitlePacket);
        }

        return packets;

    }

    private void sendPacket(Player player, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            Throwables.propagate(e);
        }
    }

    private void sendPackets(Player player, PacketContainer... packets) {
        for (PacketContainer c : packets) {
            sendPacket(player, c);
        }
    }

    private void sendPackets(Player player, List<PacketContainer> packets) {
        sendPackets(player, packets.toArray(new PacketContainer[0]));
    }
}
