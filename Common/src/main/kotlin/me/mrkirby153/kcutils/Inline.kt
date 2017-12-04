package me.mrkirby153.kcutils


/**
 * Extension for auto-closing an [AutoCloseable] as Kotlin doesn't support AutoCloseables out of the box
 *
 * @param block The code to run with the auto closeable
 */
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

/**
 * Times a function's runtime in ms
 *
 * @param block The block to run
 *
 * @return The time in ms that the code block took
 */
@Deprecated("Kotlin now has a native implementation")
inline fun timeMS(block: () -> Unit): Long {
    val start = System.currentTimeMillis()
    block.invoke()
    return System.currentTimeMillis() - start
}