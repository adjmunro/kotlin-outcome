package nz.adjmunro.knomadic.result.members

import nz.adjmunro.inline.itself
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Transform [exception][Result.Failure.exception] into some other [Throwable].
 * - [Success][Result.isSuccess] -> `returns` the original caller.
 * - [Failure][Result.isFailure] -> [wraps][failure] & `returns` the result of [onFailure] transformation.
 *
 * *Note, this function rethrows any [Throwable] exception thrown by the [onFailure] function.*
 */
@KotlinResultDsl
public inline fun <T> KotlinResult<T>.mapFailure(onFailure: (Throwable) -> Throwable): KotlinResult<T> {
    contract { callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = ::success, onFailure = { failure(onFailure(it)) })
}

/**
 * Like [map][Result.map], applies the [onSuccess] function to the encapsulated value.
 * - [Success][Result.isSuccess] -> `returns` the result of [onSuccess] transformation.
 * - [Failure][Result.isFailure] -> [wraps][failure] & `returns` the original [exception][Result.Failure.exception].
 *
 * *Note, that this function rethrows any [Throwable] exception thrown by [onSuccess] function.
 * **However**, since this is [flatMap], you can easily embed another [resultOf] inside the [onSuccess] function.*
 */
@KotlinResultDsl
public inline fun <In, Out> KotlinResult<In>.flatMap(
    @BuilderInference onSuccess: (In) -> KotlinResult<Out>,
): KotlinResult<Out> {
    return fold(onSuccess = onSuccess, onFailure = ::failure)
}

/**
 * Flattens a nested [KotlinResult].
 * - [Success][Result.isSuccess] -> `returns` the inner [KotlinResult]
 *   (which could be either a [success][Result.isSuccess] or [failure][Result.isFailure]).
 * - [Failure][Result.isFailure] -> `returns` a new [KotlinResult] with original
 *   [exception][Result.Failure.exception] from the outer [KotlinResult].
 */
@KotlinResultDsl
public fun <T> KotlinResult<KotlinResult<T>>.flatten(): KotlinResult<T> {
    return fold(onSuccess = ::itself, onFailure = ::failure)
}
