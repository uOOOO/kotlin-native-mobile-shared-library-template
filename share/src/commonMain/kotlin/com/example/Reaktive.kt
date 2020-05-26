package com.example

import com.badoo.reaktive.completable.Completable
import com.badoo.reaktive.completable.completable
import com.badoo.reaktive.single.Single
import com.badoo.reaktive.single.single
import kotlinx.coroutines.*
import kotlin.coroutines.CoroutineContext

/**
 * Refer to LaunchCoroutine.kt in Reaktive
 */
internal fun <T> singleFromCoroutineUnsafe(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> T
): Single<T> =
    single { emitter ->
        launchCoroutine(
            context = context,
            setDisposable = emitter::setDisposable,
            onSuccess = emitter::onSuccess,
            onError = emitter::onError,
            block = block
        )
    }

internal fun completableFromCoroutineUnsafe(
    context: CoroutineContext,
    block: suspend CoroutineScope.() -> Unit
): Completable =
    completable { emitter ->
        launchCoroutine(
            context = context,
            setDisposable = emitter::setDisposable,
            onSuccess = { emitter.onComplete() },
            onError = emitter::onError,
            block = block
        )
    }

// Wrapping Reaktive to RxSwift in Swift
//func toRxSingle<T>(_ single: ReaktiveSingleWrapper<T>) -> Single<T> {
//    return Single<T>.create { observer -> Disposable in
//            let disposable = single.subscribe(isThreadLocal: true, onSubscribe: nil, onError: { (kotlinThrowable) in
//            observer(.error(NSError()))
//    }) { (value) in
//            observer(.success(value!))
//    }
//        return Disposables.create { disposable.dispose() }
//    }
//}
