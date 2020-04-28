package sample

import com.badoo.reaktive.coroutinesinterop.asDisposable
import com.badoo.reaktive.single.*
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

expect class Sample() {
    fun checkMe(): Int
}

expect object Platform {
    fun name(): String
}

fun hello(): String = "Hello from ${Platform.name()}"

fun rxSingle(): SingleWrapper<String> = singleFromFunction { "ABCD" }.wrap()

fun <T> singleFromCoroutineUnsafe(mainContext: CoroutineContext, block: suspend CoroutineScope.() -> T): Single<T> =
    single { emitter ->
        GlobalScope
            .launch(mainContext) {
                try {
                    emitter.onSuccess(block())
                } catch (e: Throwable) {
                    emitter.onError(e)
                }
            }
            .asDisposable()
            .also(emitter::setDisposable)
    }

fun ktor(): SingleWrapper<String> {
    return singleFromCoroutineUnsafe(coroutineDispatcher) {
        return@singleFromCoroutineUnsafe HttpClient().get<String>("https://httpstat.us/200?sleep=5000")
    }.wrap()
}

interface TestInterface {
    fun test0()
    fun test1()
}
