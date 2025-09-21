package nz.adjmunro.knomadic.result.members

import nz.adjmunro.inline.caller
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import kotlin.Result.Companion.failure
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Transforms [value][Result.value] inside a [resultOf] scope.
 * - [Success][Result.isSuccess] -> [wraps][resultOf] & `returns` the result of [onSuccess] transformation.
 * - [Failure][Result.isFailure] -> [wraps][resultOf] & `returns` the original [Throwable].
 * - *If [onSuccess] throws an exception, it will be caught & wrapped by [resultOf].*
 *
 * ***This is the [resultOf] alternative to [Result.mapCatching].***
 *
 * ```kotlin
 * resultOf { 4 }
 *   .andThen { it * 2 } // KotlinResult.success(8)
 *   .andThen { check(false) { it } } // KotlinResult.failure(IllegalStateException("4"))
 *   .andThen { 16 } //  Remains KotlinResult.failure(IllegalStateException("4"))
 * ```
 *
 * @see resultOf
 * @see andIf
 * @see tryRecover
 */
@KotlinResultDsl
public inline fun <In, Out> KotlinResult<In>.andThen(onSuccess: (In) -> Out): KotlinResult<Out> {
    contract { callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = { resultOf { onSuccess(it) } }, onFailure = ::failure)
}

/**
 * If the [predicate] is `true`, transforms [value][Result.value] inside a [resultOf] scope.
 * - [Success][Result.isSuccess] -> [wraps][resultOf] & `returns` the result of [onSuccess] transformation.
 * - [Failure][Result.isFailure] -> [wraps][resultOf] & `returns` the original [Throwable].
 * - *If [onSuccess] throws an exception, it will be caught & wrapped by [resultOf].*
 *
 * ```kotlin
 * resultOf { 4 }.andIf({ it > 0 }) { it * 2 } // KotlinResult.success(8)
 * resultOf { 4 }.andIf({ it < 0 }) { it * 2 } // KotlinResult.success(4)
 * ```
 *
 * @see resultOf
 * @see andThen
 * @see tryRecover
 */
@KotlinResultDsl
public inline fun <T> KotlinResult<T>.andIf(
    predicate: (T) -> Boolean,
    onSuccess: (T) -> T,
): KotlinResult<T> {
    contract {
        callsInPlace(predicate, InvocationKind.AT_MOST_ONCE)
        callsInPlace(onSuccess, InvocationKind.AT_MOST_ONCE)
    }

    return andThen { if (predicate(it)) onSuccess(it) else it }
}

/**
 * Transform [exception][Result.Failure.exception] into [value][Result.value].
 * - [Success][Result.isSuccess] -> `returns` the original caller.
 * - [Failure][Result.isFailure] -> [wraps][resultOf] & `returns` the result of [onFailure] transformation.
 * - *If [onFailure] throws an exception, it will be caught & wrapped by [resultOf].*
 *
 * ***This is the [resultOf] alternative to [Result.recoverCatching].***
 *
 * ```kotlin
 * resultOf { 4 } // KotlinResult.success(4)
 *   .tryRecover { Unit } // No Change - KotlinResult.success(4)
 *   .andThen { throw FileNotFoundException("test") } // KotlinResult.failure(FileNotFoundException("test"))
 *   .tryRecover { 7 } // KotlinResult.success(7)
 * ```
 *
 * @see resultOf
 * @see andThen
 * @see mapFailure
 */
@KotlinResultDsl
public inline fun <T> KotlinResult<T>.tryRecover(onFailure: (Throwable) -> T): KotlinResult<T> {
    contract { callsInPlace(onFailure, InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = ::caller, onFailure = { resultOf { onFailure(it) } })
}
