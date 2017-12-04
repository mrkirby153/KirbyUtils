package me.mrkirby153.kcutils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

/**
 * A utility for quickly building a [TextComponent]
 */
@Suppress("NOTHING_TO_INLINE")
class TextComponentBuilder(data: String = "") {

    /**
     * The color of the text
     */
    var color = ChatColor.WHITE

    /**
     * If the text is italic
     */
    var italic = false

    /**
     * If the text is bold
     */
    var bold = false

    /**
     * If the text is underline
     */
    var underline = false

    /**
     * If the text is obfuscated (or magic)
     */
    var obfuscated = false

    /**
     * The actual text
     */
    var text = data

    /**
     * An event fired when the text is clicked
     */
    var clickEvent: ClickEvent? = null

    /**
     * An event fired when the text is hovered
     */
    var hoverEvent: HoverEvent? = null

    /**
     * Any extra [BaseComponent] for the text
     */
    val extra = mutableListOf<BaseComponent>()


    /**
     * Constructs the [TextComponent]
     *
     * @return The built [TextComponent]
     */
    fun build(): TextComponent {
        val component = TextComponent(this.text)

        component.color = this.color
        component.isItalic = this.italic
        component.isBold = this.bold
        component.isUnderlined = this.underline
        component.isObfuscated = this.obfuscated

        component.clickEvent = this.clickEvent
        component.hoverEvent = this.hoverEvent

        extra.forEach { component.addExtra(it) }
        return component
    }

    /**
     * Sends the component to a player
     *
     * @param player The player to send to
     */
    fun send(player: Player) {
        player.sendMessage(build())
    }

    /**
     * Sends the component to a command sender
     *
     * @param commandSender The sender to send the text to
     */
    fun send(commandSender: CommandSender) {
        commandSender.sendMessage(build().toLegacyText())
    }

    /**
     * Appends a [TextComponentBuilder] to the builder
     *
     * @param textComponentBuilder The builder to append
     */
    fun extra(textComponentBuilder: TextComponentBuilder): TextComponentBuilder {
        this.extra(textComponentBuilder.build())
        return this
    }

    /**
     * Appends a [BaseComponent] to the builder
     *
     * @param baseComponent The component to append
     */
    fun extra(baseComponent: BaseComponent) {
        this.extra.add(baseComponent)
    }

    /**
     * Configures this builder to link to a URL when clicked
     *
     * @param url       The URL to link to
     * @param display   Display the URL when the user hovers over it
     */
    inline fun linkTo(url: String, display: Boolean = true): TextComponentBuilder {
        if (display)
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(component("Click to visit: ").build(), component("").build(), component(url) {
                underline = true
                color = ChatColor.AQUA
            }.build()))
        clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url)
        return this
    }
}