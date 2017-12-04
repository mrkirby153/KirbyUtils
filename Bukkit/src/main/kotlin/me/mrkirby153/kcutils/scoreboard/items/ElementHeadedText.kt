package me.mrkirby153.kcutils.scoreboard.items

import me.mrkirby153.kcutils.scoreboard.ScoreboardElement

/**
 * A scoreboard element representing two lines: a header and the content
 */
class ElementHeadedText(header: String, text: String) : ScoreboardElement {

    /**
     * The header
     */
    private val header: ElementText = ElementText(header)

    /**
     * The text
     */
    private val text: ElementText = ElementText("   " + text)

    override val lines: Array<String>
        get() {
            return arrayOf(header.lines.first(), text.lines.first())
        }
}
