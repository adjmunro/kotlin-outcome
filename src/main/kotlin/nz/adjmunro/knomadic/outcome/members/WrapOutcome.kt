@file:Suppress("NOTHING_TO_INLINE")

package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Faulty
import nz.adjmunro.knomadic.outcome.Maybe
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Wrap any [T] as an [Outcome].
 * - [Throwable] and `null` are wrapped as a [Failure].
 * - Otherwise, the value is wrapped as a [Success].
 *
 * @param T The type of the value to wrap.
 * @return An [Outcome] containing the value as a [Success] or a [Failure].
 */
@OutcomeDsl
public inline val <T> T.outcome: Outcome<T & Any, Throwable>
    get() = when (this) {
        null -> NullPointerException("Value was null when wrapped as Outcome!").asFailure
        is Throwable -> asFailure
        else -> asSuccess
    }

/**
 * Wrap any [T] as an [Maybe].
 * - [Throwable] and `null` are wrapped as a [emptyFailure].
 * - Otherwise, the value is wrapped as a [Success].
 *
 * @param T The type of the value to wrap.
 * @return An [Maybe] containing the value as a [Success] or an [emptyFailure].
 */
@OutcomeDsl
public inline val <T> T.maybe: Maybe<T & Any>
    get() = when (this) {
        null -> emptyFailure()
        is Throwable -> emptyFailure()
        else -> asSuccess
    }

/**
 * Wrap any [T] as a [Faulty].
 * - [Throwable] and `null` are wrapped as a [Failure].
 * - Otherwise, an [emptySuccess] is returned.
 *
 * @param T The type of the value to wrap.
 * @return A [Faulty] containing the value as a [Failure] or an [emptySuccess].
 */
@OutcomeDsl
public inline val <T> T.faulty: Faulty<Throwable>
    get() = when (this) {
        null -> NullPointerException("Value was null when wrapped as Faulty!").asFailure
        is Throwable -> asFailure
        else -> emptySuccess()
    }

/**
 * Wraps a value of type [Ok] as an [Outcome] based on a [predicate].
 *
 * If the [predicate] returns `true`, the value is wrapped as a [Success].
 * If the [predicate] returns `false`, the [faulter] function is invoked to
 * produce an [Error] which is then wrapped as a [Failure].
 *
 * @param Ok The type of the value to wrap.
 * @param Error The type of the error produced by the [faulter].
 * @param predicate A function that determines if the value is successful.
 * @param faulter A function that produces an error if the predicate is `false`.
 * @return An [Outcome] containing the value as a [Success] or an [Error] as a [Failure].
 */
@OutcomeDsl
public fun <Ok : Any, Error: Any> Ok.outcome(
    predicate: Ok.() -> Boolean,
    faulter: Ok.() -> Error,
): Outcome<Ok, Error> {
    contract {
        callsInPlace(predicate, InvocationKind.EXACTLY_ONCE)
        callsInPlace(faulter, InvocationKind.AT_MOST_ONCE)
    }

    return if (predicate()) asSuccess else faulter().asFailure
}

/**
 * Wraps a value of type [Ok] as a [Maybe] based on a [isSuccess].
 *
 * If the [isSuccess] returns `true`, the value is wrapped as a [Success].
 * If the [isSuccess] returns `false`, an [emptyFailure] is returned.
 *
 * @param Ok The type of the value to wrap.
 * @param isSuccess A function that determines if the value is successful.
 * @return A [Maybe] containing the value as a [Success] or an [emptyFailure].
 */
@OutcomeDsl
public inline fun <Ok : Any> Ok.maybe(
    isSuccess: Ok.() -> Boolean,
): Maybe<Ok> {
    contract {
        callsInPlace(isSuccess, InvocationKind.EXACTLY_ONCE)
    }

    return if (isSuccess()) asSuccess else emptyFailure()
}

/**
 * Wraps a value of type [T] as a [Faulty] based on a [isFailure].
 *
 * If the [isFailure] returns `true`, the value is wrapped as a [Failure].
 * If the [isFailure] returns `false`, an [emptySuccess] is returned.
 *
 * @param T The type of the value to wrap.
 * @param isFailure A function that determines if the value is a failure.
 * @return A [Faulty] containing the value as a [Failure] or an [emptySuccess].
 */
@OutcomeDsl
public inline fun <T: Any> T.faulty(
    isFailure: T.() -> Boolean,
): Faulty<T> {
    contract {
        callsInPlace(isFailure, InvocationKind.EXACTLY_ONCE)
    }

    return if (isFailure()) asFailure else emptySuccess()
}
