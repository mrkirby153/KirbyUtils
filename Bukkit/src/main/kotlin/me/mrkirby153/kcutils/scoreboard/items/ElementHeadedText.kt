package me.mrkirby153.kcutils.scoreboard.items

import me.mrkirby153.kcutils.scoreboard.ScoreboardElement

class ElementHeadedText(header: String, text: String) : ScoreboardElement {

    private val header: ElementText = ElementText(header)
    private val text: ElementText = ElementText("   " + text)

    override val lines: Array<String>
        get() {
            return arrayOf(header.lines.first(), text.lines.first())
        }
}
