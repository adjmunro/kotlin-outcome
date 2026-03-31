package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.inline.caller
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Infix alias for [coerceToFailure][nz.adjmunro.outcome.members.coerceToFailure].
 *
 * Maps a [Success][nz.adjmunro.outcome.Success] value to a [Failure][nz.adjmunro.outcome.Failure] error using
 * [transform], leaving an existing [Failure][nz.adjmunro.outcome.Failure] unchanged.
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.falter(
    transform: (Ok) -> Error,
): Failure<Error> = coerceToFailure(falter = transform)

/**
 * Infix alias for [coerceToSuccess][nz.adjmunro.outcome.members.coerceToSuccess].
 *
 * Maps a [Failure][nz.adjmunro.outcome.Failure] error to a [Success][nz.adjmunro.outcome.Success] value using
 * [transform], leaving an existing [Success][nz.adjmunro.outcome.Success] unchanged.
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.recover(
    transform: (Error) -> Ok,
): Success<Ok> = coerceToSuccess(recover = transform)

/**
 * Coerces this [Outcome][nz.adjmunro.outcome.Outcome] to a [Success][nz.adjmunro.outcome.Success].
 *
 * - If already a [Success][nz.adjmunro.outcome.Success], returns it unchanged.
 * - If a [Failure][nz.adjmunro.outcome.Failure], applies [recover] to the error and wraps the result in
 *   [Success][nz.adjmunro.outcome.Success].
 *
 * > ***Warning:** No [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] is provided — exceptions thrown inside
 * > [recover] propagate uncaught. Wrap with [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] if needed.*
 *
 * @param recover Converts the [Error] value into an [Ok] value.
 *
 * @see nz.adjmunro.outcome.members.coerceToFailure
 * @see nz.adjmunro.outcome.members.recover
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.coerceToSuccess(
    recover: (Error) -> Ok,
): Success<Ok> {
    contract { callsInPlace(lambda = recover, kind = InvocationKind.AT_MOST_ONCE) }
    return fold(
        failure = { Success(value = recover(error)) },
        success = Success<Ok>::caller,
    )
}

/**
 * Coerces this [Outcome][nz.adjmunro.outcome.Outcome] to a [Failure][nz.adjmunro.outcome.Failure].
 *
 * - If already a [Failure][nz.adjmunro.outcome.Failure], returns it unchanged.
 * - If a [Success][nz.adjmunro.outcome.Success], applies [falter] to the value and wraps the result in
 *   [Failure][nz.adjmunro.outcome.Failure].
 *
 * > ***Warning:** No [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] is provided — exceptions thrown inside
 * > [falter] propagate uncaught. Wrap with [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] if needed.*
 *
 * @param falter Converts the [Ok] value into an [Error] value.
 *
 * @see nz.adjmunro.outcome.members.coerceToSuccess
 * @see nz.adjmunro.outcome.members.falter
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.coerceToFailure(
    falter: (Ok) -> Error,
): Failure<Error> {
    contract { callsInPlace(lambda = falter, kind = InvocationKind.AT_MOST_ONCE) }

    return fold(
        success = { Failure(error = falter(value)) },
        failure = Failure<Error>::caller,
    )
}
