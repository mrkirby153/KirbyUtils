package me.mrkirby153.kcutils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.entity.Player

/**
 * Class for constructing chat messages
 */
object Chat {

    var ACCENT_COLOR = ChatColor.GOLD
    var TAG_COLOR = ChatColor.BLUE
    var TEXT_COLOR = ChatColor.GRAY

    /**
     * Constructs an error message
     *
     * @param message The message to display in the error
     * @return A [net.md_5.bungee.api.chat.TextComponent]
     */
    @JvmStatic
    fun error(message: String): TextComponent {
        return message("Error", message)
    }

    /**
     * Generates formatted chat
     *
     * @param message The message to format
     * @param color   The color of the message
     * @param styles  Optional styles to apply to the chat
     * @return A [TextComponent]
     */
    @JvmStatic
    fun formattedChat(message: String, color: ChatColor, vararg styles: Style): TextComponent {
        val component = TextComponent(message)
        component.color = color
        for (s in styles) {
            when (s) {
                Style.BOLD -> component.isBold = true
                Style.ITALIC -> component.isItalic = true
                Style.UNDERLINE -> component.isUnderlined = true
                Style.STRIKETHROUGH -> component.isStrikethrough = true
                Style.OBFUSCATED -> component.isObfuscated = true
            }
        }
        return component
    }

    /**
     * Generates a hyperlink to a URL
     *
     * @param display   The text to display
     * @param hyperlink The hyperlink to link to
     * @param hoverText The hover text
     * @return A [TextComponent] when clicked will open the URL
     */
    @JvmStatic
    fun hyperlink(display: BaseComponent, hyperlink: String,
                  vararg hoverText: BaseComponent): TextComponent {
        val component = TextComponent(display)
        val hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, hoverText)
        val clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, hyperlink)
        component.hoverEvent = hoverEvent
        component.clickEvent = clickEvent
        return component
    }

    /**
     * Generates a legacy text error message
     *
     * @param message The message
     * @return The legacy error message
     */
    @JvmStatic
    fun legacyError(message: String): String {
        return error(message).toLegacyText()
    }

    /**
     * Generates a message
     *
     * @param tag     A tag to prepend to the message
     * @param message The message to generate
     * @return A [TextComponent] of the message
     */
    @JvmStatic
    fun message(tag: String, message: String): TextComponent {
        val component = formattedChat("$tag> ", TAG_COLOR)
        component.addExtra(formattedChat(message, TEXT_COLOR))
        return component
    }

    /**
     * Generates a message with formatted keys
     *
     * @param tag A tag to prepend to the message
     * @param message The message with `{name}` representing strings to replace
     * @param replacements  A list of replacements
     */
    @JvmStatic
    fun message(tag: String, message: String, vararg replacements: Any): BaseComponent {
        var msg = message
        val builder = StringBuilder()
        builder.append(TAG_COLOR)
        builder.append(tag)
        builder.append("> ")
        builder.append(TEXT_COLOR)
        run {
            var i = 0
            while (i < replacements.size) {
                msg = msg.replace(replacements[i].toString(),
                        ACCENT_COLOR.toString() + replacements[i + 1].toString() + TEXT_COLOR)
                i += 2
            }
        }
        builder.append(msg)
        val components = TextComponent.fromLegacyText(builder.toString())
        val component = components[0]
        for (i in 1 until components.size) {
            component.addExtra(components[i])
        }
        return component
    }

    /**
     * Generates a message with no tag, only an arrow
     *
     * @param message The message
     * @return A [TextComponent] of the message
     */
    @JvmStatic
    fun message(message: String): TextComponent {
        return message("", message)
    }

    /**
     * Send multiple messages to a player
     *
     * @param player   The player to send messages to
     * @param messages The messages to send
     */
    @JvmStatic
    fun sendMultiple(player: Player, vararg messages: BaseComponent) {
        for (m in messages) {
            player.spigot().sendMessage(m)
        }
    }

    enum class Style {
        BOLD,
        ITALIC,
        STRIKETHROUGH,
        UNDERLINE, OBFUSCATED
    }
}
