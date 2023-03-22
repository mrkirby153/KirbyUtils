package me.mrkirby153.kcutils.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.asCoroutineDispatcher
import kotlinx.coroutines.async
import kotlinx.coroutines.future.asCompletableFuture
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executor
import java.util.concurrent.ForkJoinPool
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Run the given coroutine asynchronously and return a [CompletableFuture] of its result
 */
inline fun <T> Executor.runAsync(crossinline body: suspend CoroutineScope.() -> T): CompletableFuture<T> {
    return CoroutineScope(this.asCoroutineDispatcher() + EmptyCoroutineContext).async {
        body()
    }.asCompletableFuture()
}

/**
 * Run the given coroutine asynchronously and return a [CompletableFuture] of its result
 */
inline fun <T> runAsync(crossinline body: suspend CoroutineScope.() -> T): CompletableFuture<T> {
    return ForkJoinPool.commonPool().runAsync(body)
}