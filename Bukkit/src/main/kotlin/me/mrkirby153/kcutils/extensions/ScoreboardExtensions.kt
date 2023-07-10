package me.mrkirby153.kcutils.extensions

import org.bukkit.OfflinePlayer
import org.bukkit.scoreboard.Objective

fun Objective.setScore(player: OfflinePlayer, score: Int? = null) {
    if (score == null) {
        getScore(player).resetScore()
    } else {
        getScore(player).score = score
    }
}

fun Objective.setScore(entry: String, score: Int? = null) {
    if (score == null) {
        getScore(entry).resetScore()
    } else {
        getScore(entry).score = score
    }
}