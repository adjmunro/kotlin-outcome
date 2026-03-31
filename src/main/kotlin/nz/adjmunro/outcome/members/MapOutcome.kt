package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.inline.itself
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Transforms both states of an [Outcome][nz.adjmunro.outcome.Outcome], re-wrapping each result in the
 * same kind of [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * - Unlike [fold][nz.adjmunro.outcome.members.fold], the result is always an
 *   [Outcome][nz.adjmunro.outcome.Outcome] — each lambda returns the unwrapped value/error, not the wrapper.
 * - Unlike [flatMapSuccess][nz.adjmunro.outcome.members.flatMapSuccess] /
 *   [flatMapFailure][nz.adjmunro.outcome.members.flatMapFailure], the lambdas here return plain values;
 *   the [Outcome][nz.adjmunro.outcome.Outcome] wrapper is added automatically.
 * - **No error handling is provided.**
 *
 * @param success Applied when the receiver is a [Success][nz.adjmunro.outcome.Success]; returns the new value.
 * @param failure Applied when the receiver is a [Failure][nz.adjmunro.outcome.Failure]; returns the new error.
 *
 * @see andThen
 * @see tryRecover
 * @see mapSuccess
 * @see mapFailure
 * @see flatMapSuccess
 * @see flatMapFailure
 */
public inline fun <In, Out, ErrorIn, ErrorOut> Outcome<In, ErrorIn>.map(
    failure: (ErrorIn) -> ErrorOut,
    success: (In) -> Out,
): Outcome<Out, ErrorOut> {
    contract {
        callsInPlace(lambda = success, kind = InvocationKind.AT_MOST_ONCE)
        callsInPlace(lambda = failure, kind = InvocationKind.AT_MOST_ONCE)
    }

    return fold(
        success = { Success(value = success(value)) },
        failure = { Failure(error = failure(error)) },
    )
}

/**
 * Transforms `Outcome<In, Error>` into `Outcome<Out, Error>` by applying [transform] to the
 * [Success][nz.adjmunro.outcome.Success] value.
 *
 * - [Failure][nz.adjmunro.outcome.Failure] outcomes pass through; the `Error` is re-wrapped to satisfy `Out`.
 * - Unlike [flatMapSuccess][nz.adjmunro.outcome.members.flatMapSuccess], [transform] returns a plain value —
 *   it is automatically wrapped in [Success][nz.adjmunro.outcome.Success].
 * - **No error handling is provided.**
 *
 * @param transform Converts the [Success][nz.adjmunro.outcome.Success] value from [In] to [Out].
 *
 * @see map
 * @see mapFailure
 * @see flatMapSuccess
 * @see andThenOf
 */
public inline infix fun <In, Out, Error> Outcome<In, Error>.mapSuccess(
    transform: (In) -> Out,
): Outcome<Out, Error> {
    return map(success = transform, failure = ::itself)
}

/**
 * Transforms `Outcome<Ok, ErrorIn>` into `Outcome<Ok, ErrorOut>` by applying [transform] to the
 * [Failure][nz.adjmunro.outcome.Failure] error.
 *
 * - [Success][nz.adjmunro.outcome.Success] outcomes pass through; the `Ok` is re-wrapped to satisfy `ErrorOut`.
 * - Unlike [flatMapFailure][nz.adjmunro.outcome.members.flatMapFailure], [transform] returns a plain value —
 *   it is automatically wrapped in [Failure][nz.adjmunro.outcome.Failure].
 * - **No error handling is provided.**
 *
 * @param transform Converts the [Failure][nz.adjmunro.outcome.Failure] error from [ErrorIn] to [ErrorOut].
 *
 * @see map
 * @see mapSuccess
 * @see flatMapFailure
 * @see tryRecoverOf
 */
public inline infix fun <Ok, ErrorIn, ErrorOut> Outcome<Ok, ErrorIn>.mapFailure(
    transform: (ErrorIn) -> ErrorOut,
): Outcome<Ok, ErrorOut> {
    return map(success = ::itself, failure = transform)
}

/**
 * Swaps the [Success][nz.adjmunro.outcome.Success] value and the [Failure][nz.adjmunro.outcome.Failure] error,
 * producing `Outcome<Error, Ok>`.
 */
public fun <Ok, Error> Outcome<Ok, Error>.invert(): Outcome<Error, Ok> {
    return fold(success = { Failure(error = value) }, failure = { Success(value = error) })
}
