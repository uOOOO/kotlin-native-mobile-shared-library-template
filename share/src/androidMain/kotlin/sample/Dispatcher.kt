package sample

import kotlinx.coroutines.Dispatchers
import kotlin.coroutines.CoroutineContext

internal actual val coroutineDispatcher: CoroutineContext = Dispatchers.Default