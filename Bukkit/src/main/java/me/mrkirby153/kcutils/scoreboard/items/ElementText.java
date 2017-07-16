package me.mrkirby153.kcutils.scoreboard.items;

import me.mrkirby153.kcutils.scoreboard.ScoreboardElement;

public class ElementText implements ScoreboardElement {

    private String text;

    public ElementText(String text) {
        if(text.length() < 16){
            for (int i = 0; i < 15 - text.length(); i++) {
                text += " ";
            }
        }
        this.text = text;
    }

    @Override
    public String[] getLines() {
        String[] text = new String[1];
        text[0] = this.text.substring(0, Math.min(this.text.length(), 40));
        return text;
    }
}
