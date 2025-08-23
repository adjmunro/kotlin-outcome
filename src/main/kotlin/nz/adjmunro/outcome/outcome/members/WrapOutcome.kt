@file:Suppress("NOTHING_TO_INLINE")

package nz.adjmunro.outcome.outcome.members

import kotlinx.coroutines.Deferred
import nz.adjmunro.outcome.outcome.Failure
import nz.adjmunro.outcome.outcome.Faulty
import nz.adjmunro.outcome.outcome.Maybe
import nz.adjmunro.outcome.outcome.Outcome
import nz.adjmunro.outcome.outcome.OutcomeDsl
import nz.adjmunro.outcome.outcome.Success
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
    predicate: Ok.() -> Boolean = { this !is Throwable },
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
    isSuccess: Ok.() -> Boolean = { this !is Throwable },
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
    isFailure: T.() -> Boolean = { this is Throwable },
): Faulty<T> {
    contract {
        callsInPlace(isFailure, InvocationKind.EXACTLY_ONCE)
    }

    return if (isFailure()) asFailure else emptySuccess()
}

/**
 * Awaits the result of a [Deferred] and wraps it as an [Outcome].
 *
 * If the [Deferred] completes successfully, the result is wrapped as a [Success].
 * If it fails with an exception or is cancelled, the exception is wrapped as a [Failure].
 *
 * @param T The type of the value to await.
 * @return An [Outcome] containing the awaited value or a [Failure] if an exception occurred.
 */
@OutcomeDsl
public suspend fun <T : Any> Deferred<T>.awaitOutcome(): Outcome<T, Throwable> {
    return outcomeOf(catch = ::Failure) { await() }
}

/**
 * Awaits the result of a [Deferred] and wraps it as a [Maybe].
 *
 * If the [Deferred] completes successfully, the result is wrapped as a [Success].
 * If it fails with an exception or is cancelled, an [emptyFailure] is returned.
 *
 * @param T The type of the value to await.
 * @return A [Maybe] containing the awaited value or an [emptyFailure] if an exception occurred.
 */
@OutcomeDsl
public suspend fun <T : Any> Deferred<T>.awaitMaybe(): Maybe<T> {
    return maybeOf(catch = ::emptyFailure) { await() }
}

/**
 * Awaits the result of a [Deferred] and wraps it as a [Faulty].
 *
 * If the [Deferred] completes successfully, an [emptySuccess] is returned.
 * If it fails with an exception or is cancelled, the exception is wrapped as a [Failure].
 *
 * @param T The type of the value to await.
 * @return A [Faulty] containing an empty success or a [Failure] if an exception occurred.
 */
@OutcomeDsl
public suspend fun <T : Any> Deferred<T>.awaitFaulty(): Faulty<Throwable> {
    return faultyOf(catch = ::Failure) { await() }
}
