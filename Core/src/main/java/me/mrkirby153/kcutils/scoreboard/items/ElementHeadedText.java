package me.mrkirby153.kcutils.scoreboard.items;

import me.mrkirby153.kcutils.scoreboard.ScoreboardElement;

public class ElementHeadedText implements ScoreboardElement {

    private ElementText header;
    private ElementText text;

    public ElementHeadedText(String header, String text) {
        this.header = new ElementText(header);
        this.text = new ElementText("   " + text);
    }

    @Override
    public String[] getLines() {
        String[] data = new String[2];
        data[0] = header.getLines()[0];
        data[1] = text.getLines()[0];
        return data;
    }
}
