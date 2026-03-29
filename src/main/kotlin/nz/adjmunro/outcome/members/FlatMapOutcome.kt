package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.inline.caller

/**
 * Transforms `Outcome<In, Error>` into `Outcome<Out, Error>` by applying [transform] to the
 * [Success][nz.adjmunro.outcome.Success] value.
 *
 * - [Failure][nz.adjmunro.outcome.Failure] outcomes pass through; the `Error` is re-wrapped to satisfy the new monad wrapper for `Out`.
 * - Unlike [mapSuccess][nz.adjmunro.outcome.members.mapSuccess], [transform] must return a full
 *   [Outcome][nz.adjmunro.outcome.Outcome] — the wrapper is **not** added for you.
 * - **No error handling is provided.**
 *
 * @param transform Converts the [Success][nz.adjmunro.outcome.Success] value into an
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * @see flatMapFailure
 * @see mapSuccess
 * @see andThenOf
 */
public inline infix fun <In, Out, Error> Outcome<In, Error>.flatMapSuccess(
    transform: (In) -> Outcome<Out, Error>,
): Outcome<Out, Error> = fold(success = { transform(value) }, failure = Failure<Error>::caller)

/**
 * Transforms `Outcome<Ok, ErrorIn>` into `Outcome<Ok, ErrorOut>` by applying [transform] to the
 * [Failure][nz.adjmunro.outcome.Failure] error.
 *
 * - [Success][nz.adjmunro.outcome.Success] outcomes pass through; the `Ok` is re-wrapped to satisfy the new monad wrapper for `ErrorOut`.
 * - Unlike [mapFailure][nz.adjmunro.outcome.members.mapFailure], [transform] must return a full
 *   [Outcome][nz.adjmunro.outcome.Outcome] — the wrapper is **not** added for you.
 * - **No error handling is provided.**
 *
 * @param transform Converts the [Failure][nz.adjmunro.outcome.Failure] error into an
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * @see flatMapSuccess
 * @see mapFailure
 * @see tryRecoverOf
 */
public inline infix fun <Ok, ErrorIn, ErrorOut> Outcome<Ok, ErrorIn>.flatMapFailure(
    transform: (ErrorIn) -> Outcome<Ok, ErrorOut>,
): Outcome<Ok, ErrorOut> = fold(success = Success<Ok>::caller, failure = { transform(error) })
