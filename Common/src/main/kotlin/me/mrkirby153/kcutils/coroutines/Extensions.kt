package me.mrkirby153.kcutils.coroutines

import kotlinx.coroutines.future.await
import java.util.concurrent.CompletableFuture

/**
 * Awaits the result of the given [CompletableFuture]
 */
@Deprecated(
    "Use CompletableFuture.await()",
    ReplaceWith("this.await()", "kotlinx.coroutines.future.await")
)
suspend fun <T> CompletableFuture<T>.await(): T = this.await()