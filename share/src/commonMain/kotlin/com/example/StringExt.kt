package com.example

import kotlinx.serialization.KSerializer

@ExperimentalStdlibApi
internal suspend fun <T> String.parseAsync(deserializer: KSerializer<T>): T {
    return async { jsonParser().parse(deserializer, this) }
}

@ExperimentalStdlibApi
internal fun <T> String.parse(deserializer: KSerializer<T>): T {
    return jsonParser().parse(deserializer, this)
}

//@ExperimentalStdlibApi
//internal fun <T> Single<ByteArray>.parse(deserializer: DeserializationStrategy<T>): Single<T> {
//    return threadLocal()
//        .observeOn(newThreadScheduler)
//        .map { bytes -> bytes.parse(deserializer) }
//        .observeOn(mainScheduler)
//        .threadLocal()
//}
