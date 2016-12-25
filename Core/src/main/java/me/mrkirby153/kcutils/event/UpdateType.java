package me.mrkirby153.kcutils.event;

public enum UpdateType {

    TICK(1),
    FAST(5),
    SLOW(10),
    SECOND(20),
    TWO_SECOND(40),
    MINUTE(SECOND.getUpdateTime() * 60);

    private int updateTime;

    private int last;

    UpdateType(int updateTime) {
        this.updateTime = updateTime;
    }

    public int getUpdateTime() {
        return updateTime;
    }

    public boolean elapsed(int currTick){
        if((last + updateTime) < currTick){
            last = currTick;
            return true;
        }
        return false;
    }
}
