package me.mrkirby153.kcutils.extensions

import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

/**
 * Creates a [Duration] from a specific number of ticks
 */
val Number.ticks get() = toLong().milliseconds * 50

/**
 * Gets the number of ticks for a duration
 */
val Duration.ticks get() = (inWholeMilliseconds / 50).toInt()