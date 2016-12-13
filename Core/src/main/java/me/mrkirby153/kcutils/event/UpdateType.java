package me.mrkirby153.kcutils.event;

public enum UpdateType {

    FASTEST(1),
    FAST(5),
    SLOW(40),
    SECOND(20),
    MINUTE(SECOND.getUpdateTime() * 60);

    private int updateTime;

    UpdateType(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getUpdateTime() {
        return updateTime;
    }
}
