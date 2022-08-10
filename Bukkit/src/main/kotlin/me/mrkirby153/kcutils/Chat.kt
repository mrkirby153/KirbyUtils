package me.mrkirby153.kcutils

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.ComponentLike
import net.kyori.adventure.text.event.ClickEvent
import net.kyori.adventure.text.event.HoverEvent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.kyori.adventure.text.format.TextDecoration

/**
 * Class for constructing chat messages
 */
object Chat {

    var ACCENT_COLOR: TextColor = NamedTextColor.GOLD
    var TAG_COLOR: TextColor = NamedTextColor.BLUE
    var TEXT_COLOR: TextColor = NamedTextColor.GRAY

    /**
     * Constructs an error message
     *
     * @param message The message to display in the error
     *
     * @return A [Component]
     */
    @JvmStatic
    fun error(message: String) = message("Error", message)

    /**
     * Generates formatted chat
     *
     * @param message The message to format
     * @param color   The color of the message
     * @param decoration  Optional styles to apply to the chat
     *
     * @return A [Component]
     */
    @JvmStatic
    fun formattedChat(message: String, color: TextColor, vararg decoration: TextDecoration) =
        Component.text(message).decorate(*decoration).color(color)

    /**
     * Generates a hyperlink to a URL
     *
     * @param display   The text to display
     * @param hyperlink The hyperlink to link to
     * @param hoverText The hover text
     *
     * @return A [Component] when clicked will open the URL
     */
    @JvmStatic
    fun hyperlink(
        display: Component, hyperlink: String, hoverText: Component? = null
    ) =
        display.hoverEvent(
            if (hoverText != null) HoverEvent.showText(hoverText) else HoverEvent.showText(
                Component.text("Click to open $hyperlink", NamedTextColor.AQUA)
            )
        ).clickEvent(ClickEvent.openUrl(hyperlink))

    /**
     * Generates a message
     *
     * @param tag     A tag to prepend to the message
     * @param message The message to generate
     *
     * @return A [Component] of the message
     */
    @JvmStatic
    fun message(tag: String, message: String): Component {
        val component = formattedChat("$tag> ", TAG_COLOR)
        return component.append(formattedChat(message, TEXT_COLOR))
    }

    /**
     * Generates a message
     *
     * @param tag       A tag to prepend to the message
     * @param message   The message to generate
     *
     * @return A [Component] of the message
     */
    @JvmStatic
    fun message(tag: String, message: ComponentLike) =
        formattedChat("$tag> ", TAG_COLOR).append(message)

    /**
     * Generates a message with formatted keys
     *
     * @param tag A tag to prepend to the message
     * @param message The message with `{name}` representing strings to replace
     * @param replacements  A list of replacements
     */
    @JvmStatic
    fun message(
        tag: String,
        message: String,
        vararg replacements: Pair<String, Any>
    ): Component {
        val baseComponent = formattedChat("$tag> ", TAG_COLOR)
        var msg: Component = Component.text(message, TEXT_COLOR)
        replacements.forEach { (k, v) ->
            msg = msg.replaceText {
                it.matchLiteral("{$k}")
                if (v is ComponentLike) {
                    it.replacement(v)
                } else {
                    it.replacement(Component.text(v.toString(), ACCENT_COLOR))
                }
            }
        }
        return baseComponent.append(msg)
    }

    /**
     * Generates a message with no tag, only an arrow
     *
     * @param message The message
     *
     * @return A [Component] of the message
     */
    @JvmStatic
    fun message(message: String) = message("", message)
}