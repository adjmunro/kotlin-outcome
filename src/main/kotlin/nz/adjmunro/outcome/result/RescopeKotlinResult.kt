@file:Suppress("unused", "NOTHING_TO_INLINE")

package nz.adjmunro.outcome.result

import nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.inline.caller
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Transforms the encapsulated value if this instance represents [success][kotlin.Result.isSuccess],
 * wrapping the [success] lambda in [resultOf][nz.adjmunro.outcome.result.resultOf] so that any thrown exception
 * is re-encapsulated as a [failure][kotlin.Result.isFailure] rather than propagating.
 *
 * Unlike [Result.map][kotlin.Result.map], exceptions from [success] are caught.
 * Unlike [Result.mapCatching][kotlin.Result.mapCatching],
 * [CancellationException][kotlinx.coroutines.CancellationException] is re-thrown when the coroutine is
 * no longer active.
 *
 * Failures pass through unchanged.
 *
 * ```kotlin
 * resultOf { 4 }
 *   .andThenOf { it * 2 }                 // Result.success(8)
 *   .andThenOf { check(false) { it } }    // Result.failure(IllegalStateException("8"))
 *   .andThenOf { 16 }                     // Remains Result.failure(IllegalStateException("8"))
 * ```
 *
 * @param I The encapsulated success input type.
 * @param O The output type after transformation.
 * @see nz.adjmunro.outcome.result.resultOf
 * @see nz.adjmunro.outcome.result.andThen
 * @see nz.adjmunro.outcome.result.mapFailure
 * @see nz.adjmunro.outcome.result.tryRecoverOf
 */
@EnsuresActiveCoroutine
public suspend inline fun <I, O> KotlinResult<I>.andThenOf(success: (I) -> O): KotlinResult<O> {
    contract { callsInPlace(lambda = success, kind = InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = { resultOf { success(it) } }, onFailure = ::kotlinFailure)
}

/**
 * Conditionally transforms the encapsulated value if [success][kotlin.Result.isSuccess] and [predicate] returns
 * `true`. If [predicate] returns `false`, the value is left unchanged.
 *
 * Exceptions from [success] are caught by [resultOf][nz.adjmunro.outcome.result.resultOf].
 * Failures pass through unchanged.
 *
 * ```kotlin
 * resultOf { 4 }.andThenOf({ it > 0 }) { it * 2 } // Result.success(8)
 * resultOf { 4 }.andThenOf({ it < 0 }) { it * 2 } // Result.success(4)
 * ```
 *
 * @see nz.adjmunro.outcome.result.resultOf
 * @see nz.adjmunro.outcome.result.andThenOf
 */
@EnsuresActiveCoroutine
public suspend inline fun <T> KotlinResult<T>.andThenOf(
    predicate: (T) -> Boolean,
    success: (T) -> T,
): KotlinResult<T> {
    contract {
        callsInPlace(lambda = predicate, kind = InvocationKind.AT_MOST_ONCE)
        callsInPlace(lambda = success, kind = InvocationKind.AT_MOST_ONCE)
    }

    return andThenOf { if (predicate(it)) success(it) else it }
}


/**
 * Attempts to recover from a [failure][kotlin.Result.isFailure] by transforming the encapsulated [Throwable]
 * **into a [success][kotlin.Result.isSuccess] value**, wrapping [failure] in
 * [resultOf][nz.adjmunro.outcome.result.resultOf].
 *
 * Unlike [Result.recover][kotlin.Result.recover], exceptions from [failure] are caught and re-encapsulated.
 * Unlike [Result.recoverCatching][kotlin.Result.recoverCatching],
 * [CancellationException][kotlinx.coroutines.CancellationException] is re-thrown when the coroutine is
 * no longer active.
 *
 * Successes pass through unchanged.
 *
 * ```kotlin
 * resultOf { 4 }                                       // Result.success(4)
 *   .tryRecoverOf { Unit }                             // No change — Result.success(4)
 *   .andThenOf { throw FileNotFoundException("test") } // Result.failure(FileNotFoundException("test"))
 *   .tryRecoverOf { 7 }                               // Result.success(7)
 * ```
 *
 * @see nz.adjmunro.outcome.result.resultOf
 * @see nz.adjmunro.outcome.result.tryRecover
 * @see nz.adjmunro.outcome.result.andThenOf
 * @see nz.adjmunro.outcome.result.mapFailure
 */
@EnsuresActiveCoroutine
public suspend inline fun <T> KotlinResult<T>.tryRecoverOf(failure: (Throwable) -> T): KotlinResult<T> {
    contract { callsInPlace(lambda = failure, kind = InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = ::caller, onFailure = { resultOf { failure(it) } })
}


/**
 * Transforms the encapsulated value if this instance represents [success][kotlin.Result.isSuccess],
 * wrapping the [success] lambda in [result][nz.adjmunro.outcome.result.result] so that any thrown exception
 * is re-encapsulated as a [failure][kotlin.Result.isFailure] rather than propagating.
 *
 * Unlike [Result.map][kotlin.Result.map], exceptions from [success] are caught.
 * Unlike [Result.mapCatching][kotlin.Result.mapCatching],
 * [CancellationException][kotlinx.coroutines.CancellationException] is also caught —
 * ***do not use in suspend contexts.***
 *
 * Failures pass through unchanged.
 *
 * ```kotlin
 * result { 4 }
 *   .andThen { it * 2 }                 // Result.success(8)
 *   .andThen { check(false) { it } }    // Result.failure(IllegalStateException("8"))
 *   .andThen { 16 }                     // Remains Result.failure(IllegalStateException("8"))
 * ```
 *
 * @param I The encapsulated success input type.
 * @param O The output type after transformation.
 * @see nz.adjmunro.outcome.result.result
 * @see nz.adjmunro.outcome.result.andThenOf
 * @see nz.adjmunro.outcome.result.mapFailure
 * @see nz.adjmunro.outcome.result.tryRecover
 */
@UnsafeForCoroutineCancellation
public inline fun <I, O> KotlinResult<I>.andThen(success: (I) -> O): KotlinResult<O> {
    contract { callsInPlace(lambda = success, kind = InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = { result { success(it) } }, onFailure = ::kotlinFailure)
}

/**
 * Conditionally transforms the encapsulated value if [success][kotlin.Result.isSuccess] and [predicate] returns
 * `true`. If [predicate] returns `false`, the value is left unchanged.
 *
 * Exceptions from [success] are caught by [result][nz.adjmunro.outcome.result.result].
 * ***Do not use in suspend contexts*** — [CancellationException][kotlinx.coroutines.CancellationException] will
 * be caught.
 *
 * Failures pass through unchanged.
 *
 * ```kotlin
 * result { 4 }.andIf({ it > 0 }) { it * 2 } // Result.success(8)
 * result { 4 }.andIf({ it < 0 }) { it * 2 } // Result.success(4)
 * ```
 *
 * @see nz.adjmunro.outcome.result.result
 * @see nz.adjmunro.outcome.result.andThenOf
 */
@UnsafeForCoroutineCancellation
public inline fun <T> KotlinResult<T>.andIf(
    predicate: (T) -> Boolean,
    success: (T) -> T,
): KotlinResult<T> {
    contract {
        callsInPlace(lambda = predicate, kind = InvocationKind.AT_MOST_ONCE)
        callsInPlace(lambda = success, kind = InvocationKind.AT_MOST_ONCE)
    }

    return andThen { if (predicate(it)) success(it) else it }
}


/**
 * Attempts to recover from a [failure][kotlin.Result.isFailure] by transforming the encapsulated [Throwable]
 * **into a [success][kotlin.Result.isSuccess] value**, wrapping [failure] in
 * [result][nz.adjmunro.outcome.result.result].
 *
 * Unlike [Result.recover][kotlin.Result.recover], exceptions from [failure] are caught and re-encapsulated.
 * Unlike [Result.recoverCatching][kotlin.Result.recoverCatching],
 * [CancellationException][kotlinx.coroutines.CancellationException] is also caught —
 * ***do not use in suspend contexts.***
 *
 * Successes pass through unchanged.
 *
 * ```kotlin
 * result { 4 }                                    // Result.success(4)
 *   .tryRecover { Unit }                          // No change — Result.success(4)
 *   .andThen { throw FileNotFoundException("test") } // Result.failure(FileNotFoundException("test"))
 *   .tryRecover { 7 }                             // Result.success(7)
 * ```
 *
 * @see nz.adjmunro.outcome.result.result
 * @see nz.adjmunro.outcome.result.tryRecoverOf
 * @see nz.adjmunro.outcome.result.andThen
 * @see nz.adjmunro.outcome.result.mapFailure
 */
@UnsafeForCoroutineCancellation
public inline fun <T> KotlinResult<T>.tryRecover(failure: (Throwable) -> T): KotlinResult<T> {
    contract { callsInPlace(lambda = failure, kind = InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = ::caller, onFailure = { result { failure(it) } })
}
