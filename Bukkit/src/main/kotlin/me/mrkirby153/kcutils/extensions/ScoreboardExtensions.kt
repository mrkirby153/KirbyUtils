package me.mrkirby153.kcutils.extensions

import org.bukkit.OfflinePlayer
import org.bukkit.scoreboard.Objective

/**
 * Sets the score for a [player] to [score]. Set to `null` to reset the score
 */
fun Objective.setScore(player: OfflinePlayer, score: Int? = null) {
    if (score == null) {
        getScore(player).resetScore()
    } else {
        getScore(player).score = score
    }
}

/**
 * Sets the score for an [entry] to [score]. Set to `null` to reset
 */
fun Objective.setScore(entry: String, score: Int? = null) {
    if (score == null) {
        getScore(entry).resetScore()
    } else {
        getScore(entry).score = score
    }
}