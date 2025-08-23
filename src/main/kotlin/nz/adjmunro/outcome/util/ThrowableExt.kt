package nz.adjmunro.outcome.util

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.job
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import nz.adjmunro.outcome.raise.exception.RaiseScopeLeakedException
import java.util.concurrent.CancellationException
import kotlin.contracts.contract
import kotlin.coroutines.Continuation
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.coroutineContext
import kotlin.coroutines.suspendCoroutine

/**
 * Determines if a [Throwable] should be fatal.
 *
 * ***Note:** [TimeoutCancellationException] unfortunately extends [CancellationException]
 * (used by Kotlin's structured concurrency) and is thus also be considered fatal.*
 */
public fun Throwable.isFatal(): Boolean {
    contract {
        returns(false) implies (this@isFatal !is Error)
        returns(false) implies (this@isFatal !is CancellationException)
        returns(false) implies (this@isFatal !is InterruptedException)
        returns(false) implies (this@isFatal !is RaiseScopeLeakedException)
    }

    return when (this) {
        is Error -> true
        is InterruptedException -> true
        is RaiseScopeLeakedException -> true
        is CancellationException -> {
//            coroutineContinuationContextOrNull()?.let { ensureActive() }
            false
        }
        else -> false
    }
}

/** Re-throw a [Throwable] if it is [fatal][isFatal]. */
public fun <Error : Throwable> Error.nonFatalOrThrow(): Error {
    return if (isFatal()) throw this else this
}

@OptIn(ExperimentalCoroutinesApi::class)
internal fun <In, Out> In.withParentCoroutineContext(blocking: suspend In.() -> Out?): Out? {
    return runBlocking { // this is stupid, and should be used with utmost caution
        currentCoroutineContext().job.parent?.let {
            ensureActive()
            blocking()
        }
    }
}

//public fun coroutineContinuationContextOrNull(): CoroutineContext? {
//    return withContext() {  }
//}

/**
 * Convert any [T] to a [Throwable].
 *
 * - If [T] is already a [Throwable], it is returned as-is.
 * - If [T] is `null`, a [NullPointerException] is created with the provided message.
 * - Otherwise, an [IllegalStateException] is created with the provided message.
 *
 * @param msg A function that generates a message based on the value of [T].
 * @return A [Throwable] representing the value of [T].
 */
public inline fun <T> T.asThrowable(msg: (T) -> String): Throwable {
    contract {
        callsInPlace(msg, kotlin.contracts.InvocationKind.AT_MOST_ONCE)
    }

    return when (this) {
        is Throwable -> this
        null -> NullPointerException(msg(this))
        else -> IllegalStateException(msg(this))
    }
}
