package nz.adjmunro.outcome.util

import kotlinx.coroutines.TimeoutCancellationException
import nz.adjmunro.outcome.raise.exception.RaiseScopeLeakedException
import java.util.concurrent.CancellationException
import kotlin.contracts.contract

/**
 * Determines if a [Throwable] should be fatal.
 *
 * ***Note:** [TimeoutCancellationException] unfortunately extends [CancellationException]
 * (used by Kotlin's structured concurrency) and is thus also be considered fatal.*
 */
public fun Throwable.isFatal(): Boolean {
    contract {
        returns(false) implies (this@isFatal !is AssertionError)
        returns(false) implies (this@isFatal !is CancellationException)
        returns(false) implies (this@isFatal !is InterruptedException)
        returns(false) implies (this@isFatal !is LinkageError)
        returns(false) implies (this@isFatal !is OutOfMemoryError)
        returns(false) implies (this@isFatal !is RaiseScopeLeakedException)
        returns(false) implies (this@isFatal !is StackOverflowError)
        returns(false) implies (this@isFatal !is VirtualMachineError)
    }

    return when (this) {
        is AssertionError -> true
        is CancellationException -> true
        is InterruptedException -> true
        is LinkageError -> true
        is OutOfMemoryError -> true
        is RaiseScopeLeakedException -> true
        is StackOverflowError -> true
        is VirtualMachineError -> true
        else -> false
    }
}

/** Re-throw a [Throwable] if it is [fatal][isFatal]. */
public fun <Error : Throwable> Error.nonFatalOrThrow(): Error {
    return if (isFatal()) throw this else this
}
