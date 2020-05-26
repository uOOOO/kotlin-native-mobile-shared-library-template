package com.example

import com.example.db.ThisDatabase
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.features.logging.DEFAULT
import io.ktor.client.features.logging.LogLevel
import io.ktor.client.features.logging.Logger
import io.ktor.client.features.logging.Logging
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonConfiguration
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

/**
 * This method must be called from Application or AppDelegate
 */
fun initKoin() = startKoin {
    modules(defaultModule, platformModule)
}

private val defaultModule = module {
    single(named("IO")) { IODispatcher }
    single { jsonParser() }
    single {
        HttpClient {
            install(JsonFeature) {
                serializer = KotlinxSerializer(get())
            }
            install(Logging) {
                logger = Logger.DEFAULT
                level = LogLevel.HEADERS
            }
        }
    }
    single { ThisDatabase(get()) }
}

internal fun jsonParser() = Json(
    JsonConfiguration.Stable.copy(
        ignoreUnknownKeys = true,
        isLenient = true,
        unquotedPrint = false
    )
)

internal expect val platformModule: Module
