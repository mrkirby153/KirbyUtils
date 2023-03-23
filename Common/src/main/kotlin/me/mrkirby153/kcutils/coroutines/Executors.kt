package me.mrkirby153.kcutils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.util.Collections
import java.util.WeakHashMap
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

private val scopeCache =
    Collections.synchronizedMap(WeakHashMap<CoroutineContext, CoroutineScope>())

/**
 * Gets or creates a new coroutine scope for the given [dispatcher]
 */
fun getOrCreateScope(dispatcher: CoroutineContext) = scopeCache.computeIfAbsent(dispatcher) {
    CoroutineScope(dispatcher + SupervisorJob())
}

/**
 * Run the given coroutine asynchronously and return a [CompletableFuture] of its result
 */
inline fun <T> Executor.runAsync(crossinline body: suspend CoroutineScope.() -> T): CompletableFuture<T> {
    return getOrCreateScope(this.asCoroutineDispatcher() + EmptyCoroutineContext).async {
        body()
    }.asCompletableFuture()
}

/**
 * Run the given coroutine asynchronously and return a [CompletableFuture] of its result
 */
inline fun <T> runAsync(
    dispatcher: CoroutineContext = Dispatchers.Default,
    crossinline body: suspend CoroutineScope.() -> T
): CompletableFuture<T> {
    return getOrCreateScope(dispatcher).async { body() }.asCompletableFuture()
}