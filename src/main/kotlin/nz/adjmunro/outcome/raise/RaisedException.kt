package nz.adjmunro.outcome.raise

import kotlin.coroutines.cancellation.CancellationException

@PublishedApi
internal class RaisedException(val error: Any?) : CancellationException("Raise was cancelled!") {
    override fun fillInStackTrace(): Throwable {
        stackTrace = emptyArray()
        return this
    }
}
