package me.mrkirby153.kcutils.protocollib;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.wrappers.EnumWrappers;
import com.comphenix.protocol.wrappers.WrappedChatComponent;
import com.google.common.base.Throwables;
import me.mrkirby153.kcutils.Module;
import net.md_5.bungee.api.chat.BaseComponent;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProtocolLib extends Module<JavaPlugin> {

    private ProtocolManager protocolManager;

    private boolean error = false;


    public ProtocolLib(JavaPlugin plugin) {
        super("ProtocolLib", "1.0", plugin);
    }

    /**
     * Gets if there was an issue initializing the ProtocolLib handler
     *
     * @return True if there was an error
     */
    public boolean isErrored() {
        return error;
    }

    /**
     * Sends text to the player's Action Bar
     *
     * @param player    The player to send the text to
     * @param component The text to send
     */
    public void sendActionBar(Player player, BaseComponent component) {
        PacketContainer actionBar = protocolManager.createPacket(PacketType.Play.Server.CHAT);
        try {
            actionBar.getChatTypes().write(0, EnumWrappers.ChatType.GAME_INFO);
        } catch (Exception e){
            // Ignore
        }
        try {
            actionBar.getBytes().write(0, (byte) 2);
        } catch (Exception e){
            // Ignore
        }
        actionBar.getChatComponents().write(0, WrappedChatComponent.fromText(component.toLegacyText()));
        sendPacket(player, actionBar);
    }

    /**
     * Sends a ProtocolLib {@link PacketContainer} to a player
     *
     * @param player The player to send the packet to
     * @param packet The {@link PacketContainer} to send
     */
    public void sendPacket(Player player, PacketContainer packet) {
        try {
            protocolManager.sendServerPacket(player, packet);
        } catch (InvocationTargetException e) {
            Throwables.propagate(e);
        }
    }

    /**
     * Sends multiple packets to a given player
     *
     * @param player  The player to send packets to
     * @param packets The packets to send
     */
    public void sendPackets(Player player, PacketContainer... packets) {
        Arrays.stream(packets).forEach(packet -> sendPacket(player, packet));
    }

    /**
     * Sends  a title to a player with default timings
     *
     * @param player   The player to send the title to
     * @param title    The title to send
     * @param subtitle The subtitle to send
     */
    public void title(Player player, String title, String subtitle) {
        title(player, title, subtitle, new TitleTimings(20, 20, 20));
    }

    /**
     * Sends a title to a player with custom tomings
     *
     * @param player   The player to send the title to
     * @param title    The title to send
     * @param subtitle The subtitle to send
     * @param timings  The {@link TitleTimings timings} to send
     */
    public void title(Player player, String title, String subtitle, TitleTimings timings) {
        sendPackets(player, constructTitle(title, subtitle, timings).toArray(new PacketContainer[0]));
    }

    /**
     * Constructs a series of packets to display a Title to the player
     *
     * @param title    The title
     * @param subtitle The subtitle
     * @param timings  The timings for the title/subtitle
     * @return A list of packets representing the title
     */
    private List<PacketContainer> constructTitle(String title, String subtitle, TitleTimings timings) {
        List<PacketContainer> packets = new ArrayList<>();

        PacketContainer timingsPacket = protocolManager.createPacket(PacketType.Play.Server.TITLE);
        timingsPacket.getTitleActions().write(0, EnumWrappers.TitleAction.TIMES);

        timingsPacket.getIntegers().write(0, timings.getFadeInTicks());
        timingsPacket.getIntegers().write(1, timings.getStayTicks());
        timingsPacket.getIntegers().write(2, timings.getFadeOutTicks());

        packets.add(timingsPacket);


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

    @Override
    protected void init() {
        if (getPlugin().getServer().getPluginManager().getPlugin("ProtocolLib") == null) {
            log("ProtocolLib is not installed or not loaded. This module will not work correctly!");
            error = true;
            return;
        }
        this.protocolManager = ProtocolLibrary.getProtocolManager();
    }
}
