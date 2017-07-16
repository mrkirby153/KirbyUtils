package me.mrkirby153.kcutils

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.BaseComponent
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@Suppress("NOTHING_TO_INLINE")
class TextComponentBuilder(data: String = "") {

    var color = ChatColor.WHITE
    var italic = false
    var bold = false
    var underline = false
    var obfuscated = false
    var text = data

    var clickEvent: ClickEvent? = null
    var hoverEvent: HoverEvent? = null

    val extra = mutableListOf<BaseComponent>()


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

    fun send(player: Player) {
        player.sendMessage(build())
    }

    fun send(commandSender: CommandSender) {
        commandSender.sendMessage(build().toLegacyText())
    }

    fun extra(textComponentBuilder: TextComponentBuilder): TextComponentBuilder {
        this.extra(textComponentBuilder.build())
        return this
    }

    fun extra(baseComponent: BaseComponent) {
        this.extra.add(baseComponent)
    }

    inline fun linkTo(url: String, display: Boolean = true): TextComponentBuilder {
        if(display)
            hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, arrayOf(component("Click to visit: ").build(), component("").build(), component(url){
                underline = true
                color = ChatColor.AQUA
            }.build()))
        clickEvent = ClickEvent(ClickEvent.Action.OPEN_URL, url)
        return this
    }
}