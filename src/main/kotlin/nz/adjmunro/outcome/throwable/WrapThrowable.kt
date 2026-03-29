package nz.adjmunro.outcome.throwable

import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Implemented by types that wrap a [Throwable], enabling transparent
 * [asThrowable][nz.adjmunro.outcome.throwable.asThrowable] conversion.
 *
 * When [asThrowable][nz.adjmunro.outcome.throwable.asThrowable] is called on a
 * [ThrowableWrapper][nz.adjmunro.outcome.throwable.ThrowableWrapper], [cause] is returned directly
 * rather than wrapping it in a new exception.
 */
public interface ThrowableWrapper<out T: Throwable> {

    /** The underlying [Throwable] being wrapped. */
    public val cause: T

}

/**
 * Converts any value of type [T] to a [Throwable]:
 *
 * - If the receiver is already a [Throwable], it is returned as-is.
 * - If the receiver implements [ThrowableWrapper][nz.adjmunro.outcome.throwable.ThrowableWrapper],
 *   its [cause][nz.adjmunro.outcome.throwable.ThrowableWrapper.cause] is returned.
 * - If the receiver is `null`, a [NullPointerException] is created using [message].
 * - Otherwise, an [IllegalStateException] is created using [message].
 *
 * @param message Produces the exception message from the receiver value. Defaults to a generic description.
 * @return A [Throwable] representing [T].
 */
public inline fun <T> T.asThrowable(
    message: (T) -> String = { "$it was converted into a Throwable!" },
): Throwable {
    contract { callsInPlace(lambda = message, kind = InvocationKind.AT_MOST_ONCE) }

    return when (this) {
        is Throwable -> this
        is ThrowableWrapper<*> -> cause
        null -> NullPointerException(message(this))
        else -> IllegalStateException(message(this))
    }
}
