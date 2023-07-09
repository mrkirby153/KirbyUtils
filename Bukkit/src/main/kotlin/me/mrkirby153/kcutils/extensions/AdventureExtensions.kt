package me.mrkirby153.kcutils.extensions

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextDecoration
import net.kyori.adventure.text.minimessage.MiniMessage
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver

/**
 * Applies bold text decorations
 */
fun Component.bold(value: Boolean = true) = decoration(TextDecoration.BOLD, value)

/**
 * Applies italic text decorations
 */
fun Component.italic(value: Boolean = true) = decoration(TextDecoration.ITALIC, value)

/**
 * Applies underline italic text decorations
 */
fun Component.underline(value: Boolean = true) = decoration(TextDecoration.UNDERLINED, value)

/**
 * Applies strikethrough text decorations
 */
fun Component.strikethrough(value: Boolean = true) = decoration(TextDecoration.STRIKETHROUGH, value)

/**
 * Applies obfuscated text decorations
 */
fun Component.obfuscated(value: Boolean = true) = decoration(TextDecoration.OBFUSCATED, value)

fun miniMessage(string: String, resolver: TagResolver = TagResolver.standard()) =
    MiniMessage.miniMessage().deserialize(string, resolver)


/**
 * Converts a string to an Adventure [Component]
 */
fun String.toComponent() = Component.text(this)

/**
 * Appends two components
 */
operator fun Component.plus(other: Component) = append(other)

/**
 * Converts a string to a component and appends it
 */
operator fun Component.plus(other: String) = this + other.toComponent()

operator fun Component.plusAssign(other: Component) {
    error("Cannot plus assign components")
}


operator fun Component.plusAssign(other: String) {
    error("Cannot plus assign components")
}