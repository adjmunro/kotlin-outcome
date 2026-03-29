package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.inline.rethrow
import nz.adjmunro.outcome.raise.DefaultRaiseScope
import nz.adjmunro.outcome.raise.RaiseScope

/**
 * Transforms the [Success][nz.adjmunro.outcome.Success] value of this [Outcome][nz.adjmunro.outcome.Outcome],
 * wrapping the result with [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] error-catching semantics.
 *
 * - [Success][nz.adjmunro.outcome.Outcome.isSuccess] — applies [success] and re-wraps the result.
 * - [Failure][nz.adjmunro.outcome.Outcome.isFailure] — returned unchanged.
 * - If [success] throws, the exception is re-encapsulated or re-thrown via [catch].
 *
 * ***This is the [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] alternative to
 * [mapSuccess][nz.adjmunro.outcome.members.mapSuccess] — it catches exceptions rather than letting them propagate.***
 *
 * ```kotlin
 * outcomeOf { 4 }
 *   .andThenOf { it * 2 }                   // Success(8)
 *   .andThenOf { check(false) { "$it" } }   // Failure(IllegalStateException("8"))
 *   .andThenOf { 16 }                       // Remains Failure(IllegalStateException("8"))
 * ```
 *
 * @param In The input [Success][nz.adjmunro.outcome.Success] value type.
 * @param Out The output [Success][nz.adjmunro.outcome.Success] value type after transformation.
 * @param Error The [Failure][nz.adjmunro.outcome.Failure] error type.
 * @param catch Handles exceptions thrown by [success]. Defaults to rethrowing via
 *   [rethrow][nz.adjmunro.outcome.inline.rethrow].
 * @param scope The [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] used to raise typed errors.
 * @param success The suspend transform applied to the [Success][nz.adjmunro.outcome.Success] value.
 * @see tryRecoverOf
 * @see mapSuccess
 * @see mapFailure
 */
@EnsuresActiveCoroutine
public suspend inline fun <In, Out, Error> Outcome<In, Error>.andThenOf(
    catch: (throwable: Throwable) -> Outcome<Out, Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    success: suspend RaiseScope<Error>.(In) -> Out,
): Outcome<Out, Error> = flatMapSuccess { ok: In ->
    outcomeOf(catch = catch, scope = scope) { success(ok) }
}

/**
 * Conditionally transforms the [Success][nz.adjmunro.outcome.Success] value, but only when [predicate] returns `true`.
 * If [predicate] returns `false`, the [Success][nz.adjmunro.outcome.Success] is returned unchanged.
 *
 * - [Failure][nz.adjmunro.outcome.Outcome.isFailure] — returned unchanged regardless of [predicate].
 * - If [success] throws, the exception is re-encapsulated or re-thrown via [catch].
 *
 * ```kotlin
 * outcomeOf { 4 }.andThenOf({ it > 0 }) { it * 2 } // Success(8)
 * outcomeOf { 4 }.andThenOf({ it < 0 }) { it * 2 } // Success(4) — predicate false, unchanged
 * ```
 *
 * @param Ok The [Success][nz.adjmunro.outcome.Success] value type.
 * @param Error The [Failure][nz.adjmunro.outcome.Failure] error type.
 * @param predicate Guards the transformation. If `false`, [success] is skipped.
 * @param catch Handles exceptions thrown by [success]. Defaults to rethrowing via
 *   [rethrow][nz.adjmunro.outcome.inline.rethrow].
 * @param scope The [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] used to raise typed errors.
 * @param success The transform applied when [predicate] is `true`.
 * @see andThenOf
 */
@EnsuresActiveCoroutine
public suspend inline fun <Ok, Error> Outcome<Ok, Error>.andThenOf(
    predicate: (Ok) -> Boolean,
    catch: (throwable: Throwable) -> Outcome<Ok, Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    success: RaiseScope<Error>.(Ok) -> Ok,
): Outcome<Ok, Error> = andThenOf(catch = catch, scope = scope) { ok: Ok ->
    if (predicate(ok)) success(ok) else ok
}


/**
 * Transforms a [Failure][nz.adjmunro.outcome.Failure] into a [Success][nz.adjmunro.outcome.Success],
 * wrapping the result with [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] error-catching semantics.
 *
 * - [Success][nz.adjmunro.outcome.Outcome.isSuccess] — returned unchanged.
 * - [Failure][nz.adjmunro.outcome.Outcome.isFailure] — applies [failure] and re-wraps the result.
 * - If [failure] throws, the exception is re-encapsulated or re-thrown via [catch].
 *
 * ***This is the [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] alternative to
 * [recover][nz.adjmunro.outcome.members.recover] — it catches exceptions rather than letting them propagate.***
 *
 * ```kotlin
 * outcomeOf { 4 }                              // Success(4)
 *   .tryRecoverOf { Unit }                     // No change — Success(4)
 *   .andThenOf { throw FileNotFoundException("test") } // Failure(FileNotFoundException("test"))
 *   .tryRecoverOf { 7 }                        // Success(7)
 * ```
 *
 * @param Ok The [Success][nz.adjmunro.outcome.Success] value type.
 * @param ErrorIn The incoming [Failure][nz.adjmunro.outcome.Failure] error type.
 * @param ErrorOut The outgoing [Failure][nz.adjmunro.outcome.Failure] error type after transformation.
 * @param catch Handles exceptions thrown by [failure]. Defaults to rethrowing via
 *   [rethrow][nz.adjmunro.outcome.inline.rethrow].
 * @param scope The [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] used to raise typed errors.
 * @param failure The transform applied to the [Failure][nz.adjmunro.outcome.Failure] error value.
 * @see andThenOf
 * @see mapFailure
 * @see recover
 */
@EnsuresActiveCoroutine
public suspend inline fun <Ok, ErrorIn, ErrorOut> Outcome<Ok, ErrorIn>.tryRecoverOf(
    catch: (throwable: Throwable) -> Outcome<Ok, ErrorOut> = ::rethrow,
    scope: RaiseScope<ErrorOut> = DefaultRaiseScope(),
    failure: RaiseScope<ErrorOut>.(ErrorIn) -> Ok,
): Outcome<Ok, ErrorOut> = flatMapFailure { e: ErrorIn ->
    outcomeOf(catch = catch, scope = scope) { failure(e) }
}


/**
 * Transforms the [Success][nz.adjmunro.outcome.Success] value of this [Outcome][nz.adjmunro.outcome.Outcome],
 * wrapping the result with [outcome][nz.adjmunro.outcome.members.outcome] error-catching semantics.
 *
 * - [Success][nz.adjmunro.outcome.Outcome.isSuccess] — applies [success] and re-wraps the result.
 * - [Failure][nz.adjmunro.outcome.Outcome.isFailure] — returned unchanged.
 * - If [success] throws, the exception is re-encapsulated or re-thrown via [catch].
 *
 * ***This is the [outcome][nz.adjmunro.outcome.members.outcome] (non-suspend) alternative to
 * [mapSuccess][nz.adjmunro.outcome.members.mapSuccess] — it catches exceptions rather than letting them propagate.***
 *
 * ```kotlin
 * outcome { 4 }
 *   .andThen { it * 2 }                   // Success(8)
 *   .andThen { check(false) { "$it" } }   // Failure(IllegalStateException("8"))
 *   .andThen { 16 }                       // Remains Failure(IllegalStateException("8"))
 * ```
 *
 * @param In The input [Success][nz.adjmunro.outcome.Success] value type.
 * @param Out The output [Success][nz.adjmunro.outcome.Success] value type after transformation.
 * @param Error The [Failure][nz.adjmunro.outcome.Failure] error type.
 * @param catch Handles exceptions thrown by [success]. Defaults to rethrowing via
 *   [rethrow][nz.adjmunro.outcome.inline.rethrow].
 * @param scope The [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] used to raise typed errors.
 * @param success The transform applied to the [Success][nz.adjmunro.outcome.Success] value.
 * @see tryRecover
 * @see mapSuccess
 * @see mapFailure
 */
@UnsafeForCoroutineCancellation
public inline fun <In, Out, Error> Outcome<In, Error>.andThen(
    catch: (throwable: Throwable) -> Outcome<Out, Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    success: RaiseScope<Error>.(In) -> Out,
): Outcome<Out, Error> = flatMapSuccess { ok: In ->
    outcome(catch = catch, scope = scope) { success(ok) }
}

/**
 * Conditionally transforms the [Success][nz.adjmunro.outcome.Success] value, but only when [predicate] returns `true`.
 * If [predicate] returns `false`, the [Success][nz.adjmunro.outcome.Success] is returned unchanged.
 *
 * - [Failure][nz.adjmunro.outcome.Outcome.isFailure] — returned unchanged regardless of [predicate].
 * - If [success] throws, the exception is re-encapsulated or re-thrown via [catch].
 *
 * ```kotlin
 * outcome { 4 }.andThen({ it > 0 }) { it * 2 } // Success(8)
 * outcome { 4 }.andThen({ it < 0 }) { it * 2 } // Success(4) — predicate false, unchanged
 * ```
 *
 * @param Ok The [Success][nz.adjmunro.outcome.Success] value type.
 * @param Error The [Failure][nz.adjmunro.outcome.Failure] error type.
 * @param predicate Guards the transformation. If `false`, [success] is skipped.
 * @param catch Handles exceptions thrown by [success]. Defaults to rethrowing via
 *   [rethrow][nz.adjmunro.outcome.inline.rethrow].
 * @param scope The [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] used to raise typed errors.
 * @param success The transform applied when [predicate] is `true`.
 * @see andThen
 */
@UnsafeForCoroutineCancellation
public inline fun <Ok, Error> Outcome<Ok, Error>.andThen(
    predicate: (Ok) -> Boolean,
    catch: (throwable: Throwable) -> Outcome<Ok, Error> = ::rethrow,
    scope: RaiseScope<Error> = DefaultRaiseScope(),
    success: RaiseScope<Error>.(Ok) -> Ok,
): Outcome<Ok, Error> = andThen(catch = catch, scope = scope) { ok: Ok ->
    if (predicate(ok)) success(ok) else ok
}


/**
 * Transforms a [Failure][nz.adjmunro.outcome.Failure] into a [Success][nz.adjmunro.outcome.Success],
 * wrapping the result with [outcome][nz.adjmunro.outcome.members.outcome] error-catching semantics.
 *
 * - [Success][nz.adjmunro.outcome.Outcome.isSuccess] — returned unchanged.
 * - [Failure][nz.adjmunro.outcome.Outcome.isFailure] — applies [failure] and re-wraps the result.
 * - If [failure] throws, the exception is re-encapsulated or re-thrown via [catch].
 *
 * ***This is the [outcome][nz.adjmunro.outcome.members.outcome] (non-suspend) alternative to
 * [recover][nz.adjmunro.outcome.members.recover] — it catches exceptions rather than letting them propagate.***
 *
 * ```kotlin
 * outcome { 4 }                              // Success(4)
 *   .tryRecover { Unit }                     // No change — Success(4)
 *   .andThen { throw FileNotFoundException("test") } // Failure(FileNotFoundException("test"))
 *   .tryRecover { 7 }                        // Success(7)
 * ```
 *
 * @param Ok The [Success][nz.adjmunro.outcome.Success] value type.
 * @param ErrorIn The incoming [Failure][nz.adjmunro.outcome.Failure] error type.
 * @param ErrorOut The outgoing [Failure][nz.adjmunro.outcome.Failure] error type after transformation.
 * @param catch Handles exceptions thrown by [failure]. Defaults to rethrowing via
 *   [rethrow][nz.adjmunro.outcome.inline.rethrow].
 * @param scope The [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] used to raise typed errors.
 * @param failure The transform applied to the [Failure][nz.adjmunro.outcome.Failure] error value.
 * @see andThen
 * @see mapFailure
 * @see recover
 */
@UnsafeForCoroutineCancellation
public inline fun <Ok, ErrorIn, ErrorOut> Outcome<Ok, ErrorIn>.tryRecover(
    catch: (throwable: Throwable) -> Outcome<Ok, ErrorOut> = ::rethrow,
    scope: RaiseScope<ErrorOut> = DefaultRaiseScope(),
    failure: RaiseScope<ErrorOut>.(ErrorIn) -> Ok,
): Outcome<Ok, ErrorOut> = flatMapFailure { e: ErrorIn ->
    outcome(catch = catch, scope = scope) { failure(e) }
}
