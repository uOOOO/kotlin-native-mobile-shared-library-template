package com.example

import com.badoo.reaktive.disposable.Disposable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.coroutines.CoroutineContext

internal suspend inline fun <T, R> T.launchCoroutine(
    context: CoroutineContext = Dispatchers.Default,
    crossinline block: T.() -> R
): R {
    return withContext(context) { block() }
}

internal expect inline fun <T> launchCoroutine(
    context: CoroutineContext,
    setDisposable: (Disposable) -> Unit,
    crossinline onSuccess: (T) -> Unit,
    crossinline onError: (Throwable) -> Unit,
    crossinline block: suspend CoroutineScope.() -> T
)
