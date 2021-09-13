package com.example

import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal actual val IODispatcher: CoroutineContext = Dispatchers.IO

internal actual fun isMainThread(): Boolean {
    return Looper.myLooper() == Looper.getMainLooper()
}
