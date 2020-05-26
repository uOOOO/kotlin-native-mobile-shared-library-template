package com.example

import com.example.db.ThisDatabase
import com.squareup.sqldelight.android.AndroidSqliteDriver
import com.squareup.sqldelight.db.SqlDriver
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single<SqlDriver> { AndroidSqliteDriver(ThisDatabase.Schema, get(), "ThisDatabase") }
}
