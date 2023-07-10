package me.mrkirby153.kcutils.legacyscoreboard.items

import me.mrkirby153.kcutils.legacyscoreboard.ScoreboardElement

/**
 * A scoreboard element consisting of one line of text
 */
class ElementText(text: String) : ScoreboardElement {

    /**
     * The text
     */
    private val text: String

    init {
        var t = text
        if (t.length < 16) {
            for (i in 0 until 15 - t.length) {
                t += " "
            }
        }
        this.text = t
    }

    override val lines: Array<String>
        get() = arrayOf(this.text.substring(0, Math.min(this.text.length, 40)))
}
