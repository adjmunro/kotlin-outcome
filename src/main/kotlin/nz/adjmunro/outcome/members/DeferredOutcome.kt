package nz.adjmunro.outcome.members

import kotlinx.coroutines.Deferred
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome

/**
 * Awaits the result of a [Deferred] and wraps it as an [Outcome].
 *
 * If the [Deferred] completes successfully, the result is wrapped as a [Success].
 * If it fails with an exception or is cancelled, the exception is wrapped as a [Failure].
 *
 * @param T The type of the value to await.
 * @return An [Outcome] containing the awaited value or a [Failure] if an exception occurred.
 */
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
public suspend fun <T : Any> Deferred<T>.awaitMaybe(): Maybe<T> {
    return maybeOf(catch = ::emptyFailure) { await() }
}

/**
 * Awaits the result of a [Deferred] and wraps it as a [Fault].
 *
 * If the [Deferred] completes successfully, an [emptySuccess] is returned.
 * If it fails with an exception or is cancelled, the exception is wrapped as a [Failure].
 *
 * @param T The type of the value to await.
 * @return A [Fault] containing an empty success or a [Failure] if an exception occurred.
 */
public suspend fun <T : Any> Deferred<T>.awaitFault(): Fault<Throwable> {
    return faultOf(catch = ::Failure) { await() }
}
