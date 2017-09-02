package me.mrkirby153.kcutils.event

enum class UpdateType private constructor(val updateTime: Int) {

    TICK(1),
    FAST(5),
    SLOW(10),
    SECOND(20),
    TWO_SECOND(40),
    MINUTE(SECOND.updateTime * 60);

    private var last: Int = 0

    fun elapsed(currTick: Int): Boolean {
        if (last + updateTime < currTick) {
            last = currTick
            return true
        }
        return false
    }
}
