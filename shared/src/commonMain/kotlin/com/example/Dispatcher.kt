package com.example

import kotlin.coroutines.CoroutineContext

internal expect val IODispatcher: CoroutineContext
internal expect fun isMainThread(): Boolean
