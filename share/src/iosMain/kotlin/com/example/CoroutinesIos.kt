package com.example

import com.badoo.reaktive.disposable.Disposable
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

internal actual inline fun <T> launchCoroutine(
    context: CoroutineContext,
    setDisposable: (Disposable) -> Unit,
    crossinline onSuccess: (T) -> Unit,
    crossinline onError: (Throwable) -> Unit,
    crossinline block: suspend CoroutineScope.() -> T
) {
    val disposable = Disposable()
    setDisposable(disposable)

    try {
        // Event loop is required
        GlobalScope
            .launch(context) {
                if (disposable.isDisposed) {
                    return@launch
                }

                launchWatchdog(disposable)
                onSuccess(block())
            }
    } catch (ignored: CancellationException) {
    } catch (e: Throwable) {
        onError(e)
    }
}

/*
 * It's not allowed to freeze a Job nor Scope, looks like the only way
 * to cancel a coroutine is to check for a condition periodically
 */
private fun CoroutineScope.launchWatchdog(disposable: Disposable) {
    launch {
        while (!disposable.isDisposed) {
            delay(COROUTINE_DELAY_CHECK_MS)
        }
        this@launchWatchdog.cancel()
    }
}

private const val COROUTINE_DELAY_CHECK_MS = 100L
