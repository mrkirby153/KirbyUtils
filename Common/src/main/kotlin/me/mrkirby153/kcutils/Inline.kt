package me.mrkirby153.kcutils


inline fun <T : AutoCloseable, R> T.use(block: (T) -> R): R {
    var closed = false
    try {
        return block(this)
    } catch (e: Exception) {
        closed = true
        try {
            this.close()
        } catch (closeException: Exception) {
        }
        throw e
    } finally {
        if (!closed) {
            this.close()
        }
    }
}

inline fun timeMS(block: ()-> Unit): Long {
    val start = System.currentTimeMillis()
    block.invoke()
    return System.currentTimeMillis() - start
}