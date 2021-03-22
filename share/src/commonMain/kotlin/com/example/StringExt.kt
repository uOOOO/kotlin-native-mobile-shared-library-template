package com.example

import kotlinx.serialization.KSerializer

@ExperimentalStdlibApi
internal suspend fun <T> String.parseAsync(deserializer: KSerializer<T>): T {
    return async { jsonParser().decodeFromString(deserializer, this) }
}

@ExperimentalStdlibApi
internal fun <T> String.parse(deserializer: KSerializer<T>): T {
    return jsonParser().decodeFromString(deserializer, this)
}
