package com.example

import com.badoo.reaktive.single.*
import kotlinx.coroutines.CoroutineScope
import kotlin.coroutines.CoroutineContext

open class BaseUseCase {
//    /**
//     * For heavy response
//     */
//    @ExperimentalStdlibApi
//    internal inline fun <T> execute(
//        context: CoroutineContext,
//        deserializer: DeserializationStrategy<T>,
//        crossinline block: suspend CoroutineScope.() -> ByteArray
//    ): SingleWrapper<T> {
//        return singleFromCoroutineUnsafe(context) { block() }
//            .parse(deserializer)
//            .wrap()
//    }

    @ExperimentalStdlibApi
    internal inline fun <T : Any> execute(
        context: CoroutineContext,
        crossinline block: suspend CoroutineScope.() -> T
    ): SingleWrapper<T> {
        return singleFromCoroutineUnsafe(context) { block() }
            .wrap()
    }
}
