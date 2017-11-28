package me.mrkirby153.kcutils.scoreboard.items

import me.mrkirby153.kcutils.scoreboard.ScoreboardElement

class ElementText(text: String) : ScoreboardElement {

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
        get() {
            val text = arrayOf<String>()
            text[0] = this.text.substring(0, Math.min(this.text.length, 40))
            return text
        }
}