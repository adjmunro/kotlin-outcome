package nz.adjmunro.outcome.raise.exception

import nz.adjmunro.outcome.raise.RaiseDsl
import kotlin.coroutines.cancellation.CancellationException

@RaiseDsl @PublishedApi
internal class RaiseCancellationException(val error: Any) : CancellationException("Raise was cancelled!") {
    override fun fillInStackTrace(): Throwable {
        stackTrace = emptyArray()
        return this
    }
}
