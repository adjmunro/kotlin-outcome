package nz.adjmunro.knomadic.result.members

import nz.adjmunro.inline.itself
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl

/**
 * Try to get the exception of this [Result].
 * - [Success][Result.isSuccess] -> `throws` a [NoSuchElementException]
 * - [Failure][Result.isFailure] -> `returns` the encapsulated [Throwable]
 */
@KotlinResultDsl
public fun <T> KotlinResult<T>.exceptionOrThrow(): Throwable {
    return fold(
        onSuccess = { throw NoSuchElementException("Result is not a failure: $this") },
        onFailure = ::itself,
    )
}

/**
 * Try to get the exception of this [Result].
 * - [Success][Result.isSuccess] -> `returns` the result of [onSuccess] transformation.
 * - [Failure][Result.isFailure] -> `returns` the encapsulated [Throwable].
 *
 * *Note, that this function rethrows any [Throwable] exception thrown by [onSuccess] function.*
 */
@KotlinResultDsl
public inline fun <T> KotlinResult<T>.exceptionOrElse(onSuccess: (T) -> Throwable): Throwable {
    return fold(onSuccess = onSuccess, onFailure = ::itself)
}

/**
 * Try to get the exception of this [Result].
 * - [Success][Result.isSuccess] -> `returns` the [default] value.
 * - [Failure][Result.isFailure] -> `returns` the encapsulated [Throwable].
 */
@KotlinResultDsl
public fun <T> KotlinResult<T>.exceptionOrDefault(default: Throwable): Throwable {
    return fold(onSuccess = { default }, onFailure = ::itself)
}
