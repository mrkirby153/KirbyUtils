package me.mrkirby153.utils;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Player.class)
public class CTest {
    @Test
    public void e() throws Exception {
        Assert.assertEquals(ChatColor.BLUE + "Error> " + ChatColor.GRAY + "testing", C.e("testing").toLegacyText());
    }

    @Test
    public void formattedChat() throws Exception {
        String message = "Testing";
        String bold = ChatColor.WHITE + "" + ChatColor.BOLD + message;
        String italic = ChatColor.WHITE + "" + ChatColor.ITALIC + message;
        String underline = ChatColor.WHITE + "" + ChatColor.UNDERLINE + message;
        Assert.assertEquals(bold, C.formattedChat("Testing", ChatColor.WHITE, C.Style.BOLD).toLegacyText());
        Assert.assertEquals(italic, C.formattedChat("Testing", ChatColor.WHITE, C.Style.ITALIC).toLegacyText());
        Assert.assertEquals(underline, C.formattedChat("Testing", ChatColor.WHITE, C.Style.UNDERLINE).toLegacyText());
    }

    @Test
    public void hyperlink() throws Exception {
        TextComponent component = C.hyperlink(C.m("Testing"), "https://google.com", C.m("Visit Google"));
        Assert.assertEquals(HoverEvent.Action.SHOW_TEXT, component.getHoverEvent().getAction());
        Assert.assertEquals(ClickEvent.Action.OPEN_URL, component.getClickEvent().getAction());
    }

    @Test
    public void legacyError() throws Exception {
        Assert.assertEquals(ChatColor.BLUE + "Error> " + ChatColor.GRAY + "testing", C.e("testing").toLegacyText());
    }

    @Test
    public void m() throws Exception {
        Assert.assertEquals(ChatColor.BLUE + "> " + ChatColor.GRAY + "testing", C.m("testing").toLegacyText());
    }

    @Test
    public void m1() throws Exception {
        Assert.assertEquals(ChatColor.BLUE + "Testing> " + ChatColor.GRAY + "testing", C.m("Testing", "testing").toLegacyText());
    }

}