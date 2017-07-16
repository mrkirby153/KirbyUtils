package me.mrkirby153.kcutils.protocollib;

public class TitleTimings {

    private final int fadeInTicks;
    private final int stayTicks;
    private final int fadeOutTicks;

    public TitleTimings(int fadeInTicks, int stayTicks, int fadeOutTicks) {
        this.fadeInTicks = fadeInTicks;
        this.stayTicks = stayTicks;
        this.fadeOutTicks = fadeOutTicks;
    }

    public int getFadeInTicks() {
        return fadeInTicks;
    }

    public int getFadeOutTicks() {
        return fadeOutTicks;
    }

    public int getStayTicks() {
        return stayTicks;
    }
}
