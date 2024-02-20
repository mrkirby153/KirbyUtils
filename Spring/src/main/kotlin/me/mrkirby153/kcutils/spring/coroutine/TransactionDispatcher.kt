package me.mrkirby153.kcutils.spring.coroutine

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentLinkedQueue
import java.util.concurrent.Executor
import java.util.concurrent.Executors
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext

/**
 * A dispatcher that guarantees transactions will stay confined to a single thread
 */
class TransactionDispatcher(
    threadPoolSize: Int = 5
) : CoroutineDispatcher() {

    private val executors: ConcurrentLinkedQueue<Executor>

    init {
        val executors = mutableListOf<Executor>()
        for (i in 0..threadPoolSize) {
            executors.add(Executors.newSingleThreadExecutor {
                Thread(it).apply {
                    name =
                        if (threadPoolSize == 1) "TransactionDispatcher" else "TransactionDispatcher-$i"
                }
            })
        }
        this.executors = ConcurrentLinkedQueue(executors)
    }

    override fun dispatch(context: CoroutineContext, block: Runnable) {
        val transactionDispatchContext = context[TransactionDispatchContext]
        checkNotNull(transactionDispatchContext) { "Cannot execute in a TransactionDispatcher without a TransactionDispatchContext" }
        transactionDispatchContext.executor.execute(block)
    }

    private suspend fun acquire(): Executor {
        if (executors.isEmpty()) {
            log.trace("TransactionDispatcher pool exhausted.")
            while (executors.isEmpty()) {
                // Jitter while we wait to acquire a thread
                delay((Math.random() * 100 + 5).toLong())
            }
        }
        val executor = executors.poll() ?: acquire() // Fail-safe in case it's null somehow
        log.trace("Acquired executor {}", executor)
        return executor
    }

    private fun release(executor: Executor) {
        executors.add(executor)
    }

    /**
     * Runs the given [block] in one of the transaction dispatchers.
     */
    suspend fun <T> runInSingleThread(block: suspend CoroutineScope.() -> T): T {
        val executor = acquire()
        val dispatchContext = TransactionDispatchContext(executor)
        try {
            return withContext(this + dispatchContext) {
                block()
            }
        } finally {
            release(executor)
        }
    }
}

internal class TransactionDispatchContext(
    internal val executor: Executor
) : AbstractCoroutineContextElement(TransactionDispatchContext) {
    companion object Key : CoroutineContext.Key<TransactionDispatchContext>
}