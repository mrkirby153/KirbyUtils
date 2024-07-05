package me.mrkirby153.kcutils.spring.coroutine

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withContext
import me.mrkirby153.kcutils.spring.coroutine.CoroutineTransactionHandler.CoroutineTransaction
import mu.KotlinLogging
import org.springframework.transaction.TransactionStatus
import org.springframework.transaction.support.TransactionTemplate
import java.util.UUID
import kotlin.coroutines.AbstractCoroutineContextElement
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext


@PublishedApi
internal val log = KotlinLogging.logger { }

@PublishedApi
internal val silentExceptions = listOf(TimeoutCancellationException::class)

/**
 * Handler for transaction management in coroutines.
 *
 * It is important that models scoped in a transaction are _not_ passed between coroutine contexts.
 * This will cause lazy load errors.
 *
 * There are two ways to use this:
 *  1. Invoking [CoroutineTransactionHandler.transaction] on an instance of [CoroutineTransaction]
 *  2. Initializing a global [TransactionTemplate] by calling [CoroutineTransactionHandler.setDefault]
 *  and invoking [transaction] directly
 */
class CoroutineTransactionHandler(val template: TransactionTemplate, poolSize: Int = 5) {

    @PublishedApi
    internal val dispatcher = TransactionDispatcher(poolSize)

    /**
     * Runs the provided [block] inside of a transaction and returns its result
     */
    suspend inline fun <T> transaction(crossinline block: suspend CoroutineScope.() -> T): T {
        val existingTransaction = coroutineContext[CoroutineTransaction]
        return when {
            existingTransaction == null -> {
                dispatcher.runInSingleThread {
                    val context = CoroutineTransactionTemplate(template)
                    log.debug { "Starting new transaction: ${context.transactionUuid}" }
                    withContext(context) {
                        runTransactionally {
                            block()
                        }
                    }
                }
            }

            existingTransaction.incomplete -> {
                check(Thread.currentThread() == existingTransaction.owningThread) {
                    "Cannot use transactions across threads"
                }
                log.debug { "Re-using existing transaction ${existingTransaction.uuid}" }
                withContext(coroutineContext) {
                    block()
                }
            }

            else -> error("Attempted to start a new transaction within a completed transaction: ${existingTransaction.uuid}")
        }
    }

    /**
     * Runs the given [block] in a Spring transaction
     */
    suspend inline fun <T> runTransactionally(crossinline block: suspend CoroutineScope.(TransactionStatus) -> T): T {
        val uuid = coroutineContext.transactionUuid
        val transactionManager = coroutineContext.transactionTemplate.transactionManager
        val status = transactionManager?.getTransaction(
            coroutineContext.transactionTemplate
        ) ?: error("Could not start a transaction")
        val transaction = CoroutineTransaction(uuid = uuid, owningThread = Thread.currentThread())
        try {
            val result = withContext(transaction) {
                block(status)
            }
            if (status.isRollbackOnly) {
                log.trace { "[$uuid] Rolling back transaction due to transaction being set to rollback only" }
                transactionManager.rollback(status)
            } else {
                log.trace { "[$uuid] Committing transaction" }
                transactionManager.commit(status)
            }
            return result
        } catch (ex: Throwable) {
            if (ex::class !in silentExceptions) {
                log.error(ex) { "[$uuid] Caught exception from transaction, initiating rollback" }
            }
            transactionManager.rollback(status)
            throw ex
        } finally {
            log.trace { "[$uuid] Marking transaction as complete" }
            transaction.complete()
        }
    }

    companion object {
        /**
         * The default transaction handler
         */
        var defaultTransactionHandler: CoroutineTransactionHandler? = null
            private set

        /**
         * Sets the default transaction [template] to use
         */
        fun setDefault(template: TransactionTemplate) {
            setDefault(CoroutineTransactionHandler(template))
        }

        /**
         * Sets the default [CoroutineTransactionHandler] to use
         */
        fun setDefault(handler: CoroutineTransactionHandler) {
            if (defaultTransactionHandler != null) {
                log.warn { "Overwriting already set default transaction handler" }
            }
            defaultTransactionHandler = handler
        }
    }

    /**
     * Coroutine context for storing the status of a transaction
     */
    class CoroutineTransaction(
        private var completed: Boolean = false,
        val uuid: String = "<<UNKNOWN>>",
        val owningThread: Thread? = null
    ) :
        AbstractCoroutineContextElement(CoroutineTransaction) {
        companion object Key : CoroutineContext.Key<CoroutineTransaction>

        /**
         * Marks this transaction as completed
         */
        fun complete() {
            completed = true
        }

        /**
         * If this transaction has been completed
         */
        val incomplete: Boolean
            get() = !completed
    }

    /**
     * Coroutine context for storing a [TransactionTemplate]
     */
    class CoroutineTransactionTemplate(
        /**
         * The template used in the transaction
         */
        val template: TransactionTemplate,

        val transactionUuid: UUID = UUID.randomUUID()
    ) :
        AbstractCoroutineContextElement(CoroutineTransactionTemplate) {
        companion object Key : CoroutineContext.Key<CoroutineTransactionTemplate>
    }

    /**
     * The transaction template of the current transaction
     */
    val CoroutineContext.transactionTemplate: TransactionTemplate
        get() = get(CoroutineTransactionTemplate)?.template
            ?: error("No transaction template in context")

    /**
     * The uuid of the current transaction
     */
    val CoroutineContext.transactionUuid: String
        get() = get(CoroutineTransactionTemplate)?.transactionUuid?.toString()
            ?: error("No transaction in current context")
}

/**
 * Run the given [block] inside a transaction.
 *
 * The default transaction handler must be set by invoking [CoroutineTransactionHandler.setDefault]
 * prior to invocation
 */
suspend inline fun <T> transaction(crossinline block: suspend CoroutineScope.() -> T): T {
    checkNotNull(CoroutineTransactionHandler.defaultTransactionHandler) { "Default transaction template has not been initialized" }
    return CoroutineTransactionHandler.defaultTransactionHandler!!.transaction {
        block()
    }
}