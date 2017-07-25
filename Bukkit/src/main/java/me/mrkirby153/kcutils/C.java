package me.mrkirby153.kcutils;

import me.mrkirby153.kcutils.format.FormatKey;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Class for constructing chat messages
 */
public class C {

    private static final String FORMATTER_REGEX = "\\{[A-Za-z0-9]+}";
    public static ChatColor ACCENT_COLOR = ChatColor.GOLD;
    public static ChatColor TAG_COLOR = ChatColor.BLUE;
    public static ChatColor TEXT_COLOR = ChatColor.GRAY;

    /**
     * Constructs an error message
     *
     * @param message The message to display in the error
     * @return A {@link net.md_5.bungee.api.chat.TextComponent}
     */
    public static TextComponent e(String message) {
        return m("Error", message);
    }

    /**
     * Generates formatted chat
     *
     * @param message The message to format
     * @param color   The color of the message
     * @param styles  Optional styles to apply to the chat
     * @return A {@link TextComponent}
     */
    public static TextComponent formattedChat(String message, ChatColor color, Style... styles) {
        TextComponent component = new TextComponent(message);
        component.setColor(color);
        for (Style s : styles) {
            switch (s) {
                case BOLD:
                    component.setBold(true);
                    break;
                case ITALIC:
                    component.setItalic(true);
                    break;
                case UNDERLINE:
                    component.setUnderlined(true);
                    break;
                case STRIKETHROUGH:
                    component.setStrikethrough(true);
                    break;
                case OBFUSCATED:
                    component.setObfuscated(true);
                    break;
            }
        }
        return component;
    }

    /**
     * Generates a hyperlink to a URL
     *
     * @param display   The text to display
     * @param hyperlink The hyperlink to link to
     * @param hoverText The hover text
     * @return A {@link TextComponent} when clicked will open the URL
     */
    public static TextComponent hyperlink(BaseComponent display, String hyperlink, BaseComponent... hoverText) {
        TextComponent component = new TextComponent(display);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText);
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.OPEN_URL, hyperlink);
        component.setHoverEvent(hoverEvent);
        component.setClickEvent(clickEvent);
        return component;
    }

    /**
     * Generates a legacy text error message
     *
     * @param message The message
     * @return The legacy error message
     */
    public static String legacyError(String message) {
        return e(message).toLegacyText();
    }

    /**
     * Generates a message
     *
     * @param tag     A tag to prepend to the message
     * @param message The message to generate
     * @return A {@link TextComponent} of the message
     */
    public static TextComponent m(String tag, String message) {
        TextComponent component = formattedChat(tag + "> ", TAG_COLOR);
        component.addExtra(formattedChat(message, TEXT_COLOR));
        return component;
    }

    /**
     * Generates a message
     *
     * @param tag     A tag to prepend the message
     * @param message The message to generate
     * @param keys    The keys to be replaced in the message. A key matches the following regex: <code>{[A-Za-z0-9]+}</code>
     * @return A {@link TextComponent} of the message
     */
    public static TextComponent m(String tag, String message, FormatKey... keys) {
        TextComponent component = formattedChat(tag + "> ", TAG_COLOR);
        Matcher matcher = Pattern.compile(FORMATTER_REGEX).matcher(message);
        int startIndex = 0;

        HashMap<String, String> keyLookupTable = new HashMap<>();
        for (FormatKey k : keys) {
            keyLookupTable.put(k.getKey(), k.getValue().toString());
        }

        while (matcher.find()) {
            String m = message.substring(startIndex, matcher.start() - 1);
            component.addExtra(formattedChat(m + " ", TEXT_COLOR));
            String replacer = message.substring(matcher.start(), matcher.end()).replaceAll("\\{|}", "");

            if (keyLookupTable.containsKey(replacer)) {
                component.addExtra(formattedChat(keyLookupTable.get(replacer), ACCENT_COLOR));
            } else {
                component.addExtra(formattedChat("{" + replacer + "}", ACCENT_COLOR));
            }

            startIndex = matcher.end();
        }
        if (!message.substring(startIndex).isEmpty())
            component.addExtra(formattedChat(message.substring(startIndex), TEXT_COLOR));

        return component;
    }

    public static BaseComponent m(String tag, String message, Object... replacements) {
        StringBuilder builder = new StringBuilder();
        builder.append(TAG_COLOR);
        builder.append(tag);
        builder.append("> ");
        builder.append(TEXT_COLOR);
        for (int i = 0; i < replacements.length; i += 2) {
            message = message.replace(replacements[i].toString(), ACCENT_COLOR + replacements[i + 1].toString() + TEXT_COLOR);
        }
        builder.append(message);
        BaseComponent[] components = TextComponent.fromLegacyText(builder.toString());
        BaseComponent component = components[0];
        for (int i = 1; i < components.length; i++) {
            component.addExtra(components[i]);
        }
        return component;
    }

    /**
     * Generates a message with no tag, only an arrow
     *
     * @param message The message
     * @return A {@link TextComponent} of the message
     */
    public static TextComponent m(String message) {
        return m("", message);
    }

    /**
     * Send multiple messages to a player
     *
     * @param player   The player to send messages to
     * @param messages The messages to send
     */
    public static void sendMultiple(Player player, BaseComponent... messages) {
        for (BaseComponent m : messages) {
            player.spigot().sendMessage(m);
        }
    }

    public enum Style {
        BOLD,
        ITALIC,
        STRIKETHROUGH,
        UNDERLINE, OBFUSCATED
    }
}
