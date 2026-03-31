@file:Suppress("unused", "NOTHING_TO_INLINE")

package nz.adjmunro.outcome.result

import nz.adjmunro.outcome.inline.itself


/**
 * Returns the encapsulated [Throwable] if this instance represents [failure][kotlin.Result.isFailure].
 *
 * The mirror of [Result.getOrThrow][kotlin.Result.getOrThrow] for the failure side.
 *
 * @throws NoSuchElementException If the result is a [success][kotlin.Result.isSuccess].
 * @see nz.adjmunro.outcome.result.exceptionOrElse
 * @see nz.adjmunro.outcome.result.exceptionOrDefault
 */
public inline fun <T> KotlinResult<T>.exceptionOrThrow(): Throwable {
    return fold(
        onSuccess = { throw NoSuchElementException("KotlinResult is not a failure: $this") },
        onFailure = ::itself,
    )
}

/**
 * Returns the encapsulated [Throwable] if [failure][kotlin.Result.isFailure], or the result of [onSuccess]
 * applied to the encapsulated value if [success][kotlin.Result.isSuccess].
 *
 * Any exception thrown by [onSuccess] is rethrown directly.
 *
 * @see nz.adjmunro.outcome.result.exceptionOrThrow
 * @see nz.adjmunro.outcome.result.exceptionOrDefault
 */
public inline fun <T> KotlinResult<T>.exceptionOrElse(onSuccess: (value: T) -> Throwable): Throwable {
    return fold(onSuccess = onSuccess, onFailure = ::itself)
}

/**
 * Returns the encapsulated [Throwable] if [failure][kotlin.Result.isFailure], or [defaultError] if
 * [success][kotlin.Result.isSuccess].
 *
 * @see nz.adjmunro.outcome.result.exceptionOrThrow
 * @see nz.adjmunro.outcome.result.exceptionOrElse
 */
public inline fun <T> KotlinResult<T>.exceptionOrDefault(defaultError: Throwable): Throwable {
    return fold(onSuccess = { defaultError }, onFailure = ::itself)
}
