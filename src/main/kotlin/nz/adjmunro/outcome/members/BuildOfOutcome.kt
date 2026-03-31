package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
import nz.adjmunro.outcome.inline.rethrow
import nz.adjmunro.outcome.raise.DefaultRaiseScope
import nz.adjmunro.outcome.raise.RaiseScope
import nz.adjmunro.outcome.raise.RaiseScope.Companion.folding
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raise

/**
 * Suspend variant of [catchException][nz.adjmunro.outcome.members.catchException] builder.
 *
 * Runs [block] and wraps the result as an [Outcome][nz.adjmunro.outcome.Outcome], using [Exception] as the error type.
 * Every thrown [Exception] is captured and wrapped in a [Failure][nz.adjmunro.outcome.Failure] automatically.
 * [kotlinx.coroutines.CancellationException] is correctly propagated for structured concurrency.
 *
 * @see nz.adjmunro.outcome.members.outcomeOf
 * @see nz.adjmunro.outcome.members.catchStringOf
 */
@EnsuresActiveCoroutine
public suspend inline fun <Ok> catchExceptionOf(
    scope: RaiseScope<Exception> = DefaultRaiseScope(),
    block: suspend RaiseScope<Exception>.() -> Ok,
): Outcome<Ok, Exception> = outcomeOf(catch = ::Failure, scope = scope, block = block)

/**
 * Suspend variant of [catchString][nz.adjmunro.outcome.members.catchString] builder.
 *
 * Runs [block] and wraps the result as an [Outcome][nz.adjmunro.outcome.Outcome], using [String] as the error type.
 * Every thrown [Exception] is captured and its [Throwable.message] (or [toString] if null) is used as the error.
 * [kotlinx.coroutines.CancellationException] is correctly propagated for structured concurrency.
 *
 * @see nz.adjmunro.outcome.members.outcomeOf
 * @see nz.adjmunro.outcome.members.catchExceptionOf
 */
@EnsuresActiveCoroutine
public suspend inline fun <Ok> catchStringOf(
    scope: RaiseScope<String> = DefaultRaiseScope(),
    block: suspend RaiseScope<String>.() -> Ok,
): Outcome<Ok, String> = outcomeOf(
    catch = { e: Exception -> Failure(error = e.message ?: e.toString()) },
    scope = scope,
    block = block,
)

/**
 * Suspend variant of [fault][nz.adjmunro.outcome.members.fault] builder.
 *
 * Runs [block] inside a [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] and returns a
 * [Fault][nz.adjmunro.outcome.members.Fault]. [kotlinx.coroutines.CancellationException] is correctly propagated
 * for structured concurrency.
 *
 * The success value is always [Unit]; use [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] to
 * short-circuit with an [Error].
 *
 * ```kotlin
 * // Fault<String> — raises an error
 * val f: Fault<String> = faultOf { raise { "something went wrong" } }
 *
 * // Fault<Exception> — exception mapped via catch
 * val g: Fault<Exception> = faultOf(catch = ::Failure) { throw IllegalStateException("oops") }
 * ```
 *
 * @param catch Maps a thrown [Exception] to a [Fault][nz.adjmunro.outcome.members.Fault].
 *   Rethrows by default — override to suppress exceptions.
 * @param block The suspend block to execute within the scope.
 * @see nz.adjmunro.outcome.members.faultOf
 */
@EnsuresActiveCoroutine
public suspend inline fun <Error> faultOf(
    catch: (exception: Exception) -> Fault<Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    block: suspend RaiseScope<Error>.() -> Unit,
): Fault<Error> = outcomeOf(catch = catch, scope = scope, block = block).asFault

/**
 * Suspend variant of [maybe][nz.adjmunro.outcome.members.maybe] builder.
 *
 * Runs [block] inside a [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] and returns a
 * [Maybe][nz.adjmunro.outcome.members.Maybe]. [kotlinx.coroutines.CancellationException] is correctly propagated
 * for structured concurrency.
 *
 * The error type is always mapped to [Unit]; use [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] to
 * short-circuit with an absent value.
 *
 * ```kotlin
 * // Maybe<String> — success
 * val m: Maybe<String> = maybeOf { "hello".uppercase() }
 *
 * // Maybe<Int> — absent
 * val n: Maybe<Int> = maybeOf { raise { Unit } }
 * ```
 *
 * @param catch Maps a thrown [Exception] to a [Maybe][nz.adjmunro.outcome.members.Maybe].
 *   Returns [emptyFailure][nz.adjmunro.outcome.members.emptyFailure] by default.
 * @param block The suspend block to execute within the scope.
 * @see nz.adjmunro.outcome.members.maybeOf
 */
@EnsuresActiveCoroutine
public suspend inline fun <Ok> maybeOf(
    catch: (exception: Exception) -> Maybe<Ok> = ::emptyFailure,
    scope: RaiseScope<Any?> = DefaultRaiseScope(),
    block: suspend RaiseScope<Any?>.() -> Ok,
): Maybe<Ok> = outcomeOf(catch = catch, scope = scope, block = block).asMaybe

/**
 * Suspend variant of [outcome][nz.adjmunro.outcome.members.outcome] builder.
 *
 * Runs [block] inside a [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] and returns an
 * [Outcome][nz.adjmunro.outcome.Outcome]. [kotlinx.coroutines.CancellationException] is correctly propagated
 * for structured concurrency.
 *
 * A successful return value is wrapped in [Success][nz.adjmunro.outcome.Success].
 * A [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] call short-circuits and wraps the error in
 * [Failure][nz.adjmunro.outcome.Failure].
 *
 * > ***Note:** [catch] defaults to [rethrow]. Override it to map exceptions to an [Outcome];
 * > passing [Failure] directly forces [Error] => [Exception], which may conflict with the intended error type.*
 *
 * ```kotlin
 * // Outcome<Unit, Exception> — all exceptions captured
 * val a: Outcome<Unit, Exception> = outcomeOf(catch = ::Failure) { /* ... */  }
 *
 * // Outcome<Int, String> — raise to fail
 * val b: Outcome<Int, String> = outcomeOf {
 *     raise { "error" }
 *     42
 * }
 *
 * // Outcome<String, String> — catch inside block
 * val c: Outcome<String, String> = outcomeOf {
 *     // Catching can internally handle any Throwable, including would-be fatal Errors
 *     catching({ it.message }) { throw OutOfMemoryError() }
 * }
 * ```
 *
 * @param catch Maps a thrown [Exception] to an [Outcome][nz.adjmunro.outcome.Outcome].
 *   Rethrows by default — override to suppress exceptions.
 * @param block The suspend block to execute within the scope.
 * @see nz.adjmunro.outcome.members.catchStringOf
 * @see nz.adjmunro.outcome.members.outcome
 */
@EnsuresActiveCoroutine
public suspend inline fun <Ok, Error> outcomeOf(
    catch: (exception: Exception) -> Outcome<Ok, Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    block: suspend RaiseScope<Error>.() -> Ok,
): Outcome<Ok, Error> = scope.run {
    folding(
        block = block,
        catch = catch,
        recover = ::Failure,
        transform = ::Success,
    )
}
