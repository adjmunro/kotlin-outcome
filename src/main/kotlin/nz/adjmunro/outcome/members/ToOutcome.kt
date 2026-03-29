package nz.adjmunro.outcome.members

import kotlinx.coroutines.Deferred
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.inline.unit
import nz.adjmunro.outcome.result.KotlinResult
import nz.adjmunro.outcome.result.resultOf
import nz.adjmunro.outcome.throwable.ThrowableWrapper
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Converts any [Outcome][nz.adjmunro.outcome.Outcome] to a [Fault][nz.adjmunro.outcome.members.Fault]
 * by discarding the [Success][nz.adjmunro.outcome.Success] value (mapping it to [Unit]).
 */
public inline val <Error> Outcome<*, Error>.asFault: Fault<Error>
    get() = mapSuccess(transform = ::unit)

/**
 * Converts any [Outcome][nz.adjmunro.outcome.Outcome] to a [Maybe][nz.adjmunro.outcome.members.Maybe]
 * by discarding the [Failure][nz.adjmunro.outcome.Failure] error (mapping it to [Unit]).
 */
public inline val <Ok> Outcome<Ok, *>.asMaybe: Maybe<Ok>
    get() = mapFailure(transform = ::unit)

/**
 * Converts a [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] to an
 * [Outcome][nz.adjmunro.outcome.Outcome] with [Throwable] as the error type.
 */
public inline val <T> KotlinResult<T>.asOutcome: Outcome<T, Throwable>
    get() = fold(onSuccess = ::Success, onFailure = ::Failure)

/** Directly wraps any [T] as a [Success][nz.adjmunro.outcome.Success]. No type inference or null-checks performed. */
public inline val <T> T.asSuccess: Success<T>
    get() = Success(value = this)

/** Directly wraps any [T] as a [Failure][nz.adjmunro.outcome.Failure]. No type inference or null-checks performed. */
public inline val <T> T.asFailure: Failure<T>
    get() = Failure(error = this)

/**
 * Smart-wraps any [T] as an [Outcome][nz.adjmunro.outcome.Outcome].
 * - `null` → [Failure][nz.adjmunro.outcome.Failure] wrapping a [NullPointerException].
 * - [Throwable], [ThrowableWrapper] → [Failure][nz.adjmunro.outcome.Failure] wrapping the throwable directly.
 * - Anything else → [Success][nz.adjmunro.outcome.Success].
 *
 * Use [asSuccess][nz.adjmunro.outcome.members.asSuccess] or [asFailure][nz.adjmunro.outcome.members.asFailure]
 * for direct wrapping without type inference.
 *
 * @param T The type of the value to wrap.
 */
public inline val <T> T.toOutcome: Outcome<T & Any, Throwable>
    get() = when (this) {
        null -> NullPointerException("Value was null when wrapped as Outcome!").asFailure
        is Throwable -> asFailure
        is ThrowableWrapper<*> -> cause.asFailure
        else -> asSuccess
    }

/**
 * Smart-wraps any [T] as a [Maybe][nz.adjmunro.outcome.members.Maybe].
 * - `null` → [emptyFailure][nz.adjmunro.outcome.members.emptyFailure].
 * - [Throwable], [ThrowableWrapper] → [emptyFailure][nz.adjmunro.outcome.members.emptyFailure].
 * - Anything else → [Success][nz.adjmunro.outcome.Success].
 *
 * @param T The type of the value to wrap.
 */
public inline val <T> T.toMaybe: Maybe<T & Any>
    get() = when (this) {
        null -> emptyFailure()
        is Throwable -> emptyFailure()
        is ThrowableWrapper<*> -> emptyFailure()
        else -> asSuccess
    }

/**
 * Smart-wraps any [T] as a [Fault][nz.adjmunro.outcome.members.Fault].
 * - `null` → [Failure][nz.adjmunro.outcome.Failure] wrapping a [NullPointerException].
 * - [Throwable], [ThrowableWrapper] → [Failure][nz.adjmunro.outcome.Failure] wrapping the throwable directly.
 * - Anything else → [emptySuccess][nz.adjmunro.outcome.members.emptySuccess].
 *
 * @param T The type of the value to wrap.
 */
public inline val <T> T.toFault: Fault<Throwable>
    get() = when (this) {
        null -> NullPointerException("Value was null when wrapped as Fault!").asFailure
        is Throwable -> asFailure
        is ThrowableWrapper<*> -> cause.asFailure
        else -> emptySuccess()
    }

/**
 * Wraps this value as an [Outcome][nz.adjmunro.outcome.Outcome] based on a [predicate].
 *
 * - [predicate] returns `true` → [Success][nz.adjmunro.outcome.Success] containing this value.
 * - [predicate] returns `false` → [Failure][nz.adjmunro.outcome.Failure] produced by [faulter].
 *
 * @param Ok The type of the value to wrap.
 * @param Error The error type produced by [faulter].
 * @param predicate Determines whether the value represents success.
 * @param faulter Produces the [Failure][nz.adjmunro.outcome.Failure] error when [predicate] is `false`.
 *   Called at most once.
 */
public fun <Ok, Error> Ok.toOutcome(
    predicate: Ok.() -> Boolean,
    faulter: Ok.() -> Error,
): Outcome<Ok, Error> {
    contract {
        callsInPlace(lambda = predicate, kind = InvocationKind.EXACTLY_ONCE)
        callsInPlace(lambda = faulter, kind = InvocationKind.AT_MOST_ONCE)
    }

    return if (predicate()) asSuccess else faulter().asFailure
}

/**
 * Wraps this value as a [Maybe][nz.adjmunro.outcome.members.Maybe] based on [isSuccess].
 *
 * - [isSuccess] returns `true` → [Success][nz.adjmunro.outcome.Success] containing this value.
 * - [isSuccess] returns `false` → [emptyFailure][nz.adjmunro.outcome.members.emptyFailure].
 *
 * @param Ok The type of the value to wrap.
 * @param isSuccess Determines whether the value represents success.
 */
public inline fun <Ok> Ok.toMaybe(
    isSuccess: Ok.() -> Boolean,
): Maybe<Ok> {
    contract {
        callsInPlace(lambda = isSuccess, kind = InvocationKind.EXACTLY_ONCE)
    }

    return if (isSuccess()) asSuccess else emptyFailure()
}

/**
 * Wraps this value as a [Fault][nz.adjmunro.outcome.members.Fault] based on [isFailure].
 *
 * - [isFailure] returns `true` → [Failure][nz.adjmunro.outcome.Failure] containing this value.
 * - [isFailure] returns `false` → [emptySuccess][nz.adjmunro.outcome.members.emptySuccess].
 *
 * @param T The type of the value to wrap.
 * @param isFailure Determines whether the value represents a failure.
 */
public inline fun <T> T.toFault(
    isFailure: T.() -> Boolean,
): Fault<T> {
    contract {
        callsInPlace(lambda = isFailure, kind = InvocationKind.EXACTLY_ONCE)
    }

    return if (isFailure()) asFailure else emptySuccess()
}
