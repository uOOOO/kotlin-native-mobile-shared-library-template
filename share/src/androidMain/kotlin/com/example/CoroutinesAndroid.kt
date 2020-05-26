package com.example

import com.badoo.reaktive.coroutinesinterop.asDisposable
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
    GlobalScope
        .launch(context) {
            try {
                onSuccess(block())
            } catch (ignored: CancellationException) {
            } catch (e: Throwable) {
                onError(e)
            }
        }
        .asDisposable()
        .also(setDisposable)
}
