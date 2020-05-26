package com.example

import kotlinx.serialization.DeserializationStrategy

@ExperimentalStdlibApi
internal fun <T> ByteArray.parse(deserializer: DeserializationStrategy<T>): T {
    return jsonParser().parse(deserializer, decodeToString())
}

//@ExperimentalStdlibApi
//internal fun <T> Single<ByteArray>.parse(deserializer: DeserializationStrategy<T>): Single<T> {
//    return threadLocal()
//        .observeOn(newThreadScheduler)
//        .map { bytes -> bytes.parse(deserializer) }
//        .observeOn(mainScheduler)
//        .threadLocal()
//}
