@file:Suppress("unused", "NOTHING_TO_INLINE")

package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.inline.rethrow
import nz.adjmunro.outcome.raise.DefaultRaiseScope
import nz.adjmunro.outcome.raise.RaiseScope
import nz.adjmunro.outcome.raise.RaiseScope.Companion.fold
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raise
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns a [Failure][nz.adjmunro.outcome.Failure] whose error is [Unit].
 * Convenient when a [Maybe][nz.adjmunro.outcome.members.Maybe] must signal absence without providing an error value.
 */
public inline fun emptyFailure(ignore: Any? = null): Failure<Unit> = Failure(error = Unit)

/**
 * Returns a [Success][nz.adjmunro.outcome.Success] whose value is [Unit].
 * Convenient when a [Fault][nz.adjmunro.outcome.members.Fault] must signal success without providing a value.
 */
public inline fun emptySuccess(ignore: Any? = null): Success<Unit> = Success(value = Unit)


/**
 * Builds a [Failure][nz.adjmunro.outcome.Failure] whose error is the value returned by [block].
 *
 * @param block Returns the error value.
 */
public inline fun <Error> failure(block: () -> Error): Failure<Error> {
    contract { callsInPlace(lambda = block, kind = InvocationKind.EXACTLY_ONCE) }
    return Failure(error = block())
}

/**
 * Builds a [Success][nz.adjmunro.outcome.Success] whose value is the value returned by [block].
 *
 * @param block Returns the success value.
 */
public inline fun <Ok> success(block: () -> Ok): Success<Ok> {
    contract { callsInPlace(lambda = block, kind = InvocationKind.EXACTLY_ONCE) }
    return Success(value = block())
}

/**
 * Runs [block] and wraps the result as an [Outcome][nz.adjmunro.outcome.Outcome], using [Exception] as the error type.
 *
 * Every thrown [Exception] is captured and wrapped in a [Failure][nz.adjmunro.outcome.Failure] automatically.
 * Use this when any exception is an acceptable error signal, and you don't need a narrower error type.
 *
 * > ***Warning:** Does not propagate [kotlinx.coroutines.CancellationException] — use
 * > [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] in suspend contexts.*
 *
 * @see nz.adjmunro.outcome.members.outcome
 * @see nz.adjmunro.outcome.members.catchString
 */
@UnsafeForCoroutineCancellation
public inline fun <Ok> catchException(
    scope: RaiseScope<Exception> = DefaultRaiseScope(),
    block: RaiseScope<Exception>.() -> Ok,
): Outcome<Ok, Exception> = outcome(catch = ::Failure, scope = scope, block = block)

/**
 * Runs [block] and wraps the result as an [Outcome][nz.adjmunro.outcome.Outcome], using [String] as the error type.
 *
 * Every thrown [Exception] is captured and its [Throwable.message] (or [toString] if null) is used as the error.
 * Useful for quick prototyping or when a human-readable message is sufficient.
 *
 * > ***Warning:** Does not propagate [kotlinx.coroutines.CancellationException] — use
 * > [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] in suspend contexts.*
 *
 * @see nz.adjmunro.outcome.members.outcome
 * @see nz.adjmunro.outcome.members.catchException
 */
@UnsafeForCoroutineCancellation
public inline fun <Ok> catchString(
    scope: RaiseScope<String> = DefaultRaiseScope(),
    block: RaiseScope<String>.() -> Ok,
): Outcome<Ok, String> = outcome(
    catch = { e: Exception -> Failure(error = e.message ?: e.toString()) },
    scope = scope,
    block = block
)

/**
 * Runs [block] inside a [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] and returns a
 * [Fault][nz.adjmunro.outcome.members.Fault].
 *
 * The success value is always [Unit]; use [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] to
 * short-circuit with an [Error].
 *
 * > ***Warning:** Does not propagate [kotlinx.coroutines.CancellationException] — use
 * > [faultOf][nz.adjmunro.outcome.members.outcomeOf] in suspend contexts.*
 *
 * ```kotlin
 * // Fault<String> — raises an error
 * val f: Fault<String> = fault { raise { "something went wrong" } }
 *
 * // Fault<Exception> — exception mapped via catch
 * val g: Fault<Exception> = fault(catch = ::Failure) {
 *     throw IllegalStateException("oops")
 * }
 * ```
 *
 * @param catch Maps a thrown [Exception] to a [Fault][nz.adjmunro.outcome.members.Fault].
 *              Rethrows by default — override to suppress exceptions.
 * @param block The block to execute within the scope.
 * @see nz.adjmunro.outcome.members.faultOf
 */
@UnsafeForCoroutineCancellation
public inline fun <Error> fault(
    catch: (exception: Exception) -> Fault<Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    block: RaiseScope<Error>.() -> Unit,
): Fault<Error> = outcome(catch = catch, scope = scope, block = block).asFault

/**
 * Runs [block] inside a [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] and returns a
 * [Maybe][nz.adjmunro.outcome.members.Maybe].
 *
 * The error type is always mapped to [Unit]; use [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] to
 * short-circuit with an absent value.
 *
 * > ***Warning:** Does not propagate [kotlinx.coroutines.CancellationException] — use
 * > [maybeOf][nz.adjmunro.outcome.members.outcomeOf] in suspend contexts.*
 *
 * ```kotlin
 * // Maybe<String> — success
 * val m: Maybe<String> = maybe { "hello".uppercase() }
 *
 * // Maybe<Int> — absent
 * val n: Maybe<Int> = maybe { raise { Unit } }
 * ```
 *
 * @param catch Maps a thrown [Exception] to a [Maybe][nz.adjmunro.outcome.members.Maybe].
 *              Returns [emptyFailure][nz.adjmunro.outcome.members.emptyFailure] by default.
 * @param block The block to execute within the scope.
 * @see nz.adjmunro.outcome.members.maybeOf
 */
@UnsafeForCoroutineCancellation
public inline fun <Ok> maybe(
    catch: (exception: Exception) -> Maybe<Ok> = ::emptyFailure,
    scope: RaiseScope<Any?> = DefaultRaiseScope(),
    block: RaiseScope<Any?>.() -> Ok,
): Maybe<Ok> = outcome(catch = catch, scope = scope, block = block).asMaybe

/**
 * Runs [block] inside a [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] and returns an
 * [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * A successful return value is wrapped in [Success][nz.adjmunro.outcome.Success].
 * A [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] call short-circuits and wraps the error in
 * [Failure][nz.adjmunro.outcome.Failure].
 *
 * > ***Warning:** Does not propagate [kotlinx.coroutines.CancellationException] — use
 * > [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] in suspend contexts.*
 *
 * > ***Note:** [catch] defaults to [rethrow]. Override it to map exceptions to an [Outcome];
 * > passing [Failure] directly forces [Error] = [Exception], which may conflict with the intended error type.*
 *
 * ```kotlin
 * // Outcome<Unit, Exception> — all exceptions captured
 * val a: Outcome<Unit, Exception> = outcome(catch = ::Failure) { /* ... */  }
 *
 * // Outcome<Int, String> — raise to fail
 * val b: Outcome<Int, String> = outcome {
 *     raise { "error" }
 *     42
 * }
 *
 * // Outcome<String, String> — catch inside block
 * val c: Outcome<String, String> = outcomeOf {
 *     // Catch can internally handle any Throwable, including would-be fatal Errors
 *     catch({ it.message }) { throw OutOfMemoryError() }
 * }
 * ```
 *
 * @param catch Maps a thrown [Exception] to an [Outcome][nz.adjmunro.outcome.Outcome].
 *              Rethrows by default — override to suppress exceptions.
 * @param block The block to execute within the scope.
 * @see nz.adjmunro.outcome.members.catchString
 * @see nz.adjmunro.outcome.members.outcomeOf
 */
@UnsafeForCoroutineCancellation
public inline fun <Ok, Error> outcome(
    catch: (exception: Exception) -> Outcome<Ok, Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    block: RaiseScope<Error>.() -> Ok,
): Outcome<Ok, Error> = scope.run {
    fold(
        block = block,
        catch = catch,
        recover = ::Failure,
        transform = ::Success,
    )
}
