package me.mrkirby153.kcutils.utils

/**
 * A generator that generates snowflakes
 */
class SnowflakeWorker(private val workerId: Long, private val nodeId: Long,
                      private var sequence: Long = 0) {

    private val workerIdBits = 5L
    private val nodeBits = 5L
    private val seqBits = 5L

    private val maxWorkerId = -1L xor (-1L shl workerIdBits.toInt())
    private val maxNodeId = -1L xor (-1L shl nodeBits.toInt())

    private val workerIdShift = seqBits
    private val nodeShift = seqBits + workerIdBits
    private val timestampShift = seqBits + workerIdBits + nodeBits
    private val seqMask = -1L xor (-1L shl seqBits.toInt())

    private var lastTimestamp = -1L

    init {
        if (workerId > maxWorkerId || workerId < 0) {
            throw IllegalArgumentException(
                    "Worker ID can't be greater than $maxWorkerId or less than 0")
        }
        if (nodeId > maxNodeId || nodeId < 0) {
            throw IllegalArgumentException(
                    "Node ID can't be greater than $maxNodeId or less than 0")
        }
    }

    /**
     * Generates a snowflake. Blocks if there are no snowflakes remaining in the current millisecond
     */
    fun generate(): Long {
        var timestamp = System.currentTimeMillis()

        if (timestamp < lastTimestamp) {
            throw IllegalArgumentException(
                    "Time ran backwards. Clock is ${lastTimestamp - timestamp}ms behind")
        }

        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) and seqMask
            if (sequence == 0L) {
                waitUntilNextMS(timestamp)
                timestamp = System.currentTimeMillis()
            }
        } else {
            sequence = 0
        }

        lastTimestamp = timestamp
        return (timestamp shl timestampShift.toInt()) or (nodeId shl nodeShift.toInt()) or (workerId shl workerIdShift.toInt()) or sequence
    }

    private fun waitUntilNextMS(last: Long) {
        var ts = System.currentTimeMillis()
        while (ts <= last) {
            ts = System.currentTimeMillis()
        }
    }
}