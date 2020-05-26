package com.example

import com.example.db.ThisDatabase
import com.squareup.sqldelight.db.SqlDriver
import com.squareup.sqldelight.drivers.native.NativeSqliteDriver
import org.koin.core.module.Module
import org.koin.dsl.module

internal actual val platformModule: Module = module {
    single<SqlDriver> { NativeSqliteDriver(ThisDatabase.Schema, "ThisDatabase") }
}
