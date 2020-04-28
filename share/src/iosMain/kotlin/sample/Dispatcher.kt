package sample

import kotlinx.coroutines.*
import platform.darwin.*
import kotlin.coroutines.CoroutineContext

internal actual val coroutineDispatcher: CoroutineContext =
    NsQueueDispatcher(dispatch_get_main_queue())

internal class NsQueueDispatcher(
    private val dispatchQueue: dispatch_queue_t
) : CoroutineDispatcher() {
    override fun dispatch(context: CoroutineContext, block: Runnable) {
        dispatch_async(dispatchQueue) {
            block.run()
        }
    }
}