package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.inline.caller
import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success
import nz.adjmunro.knomadic.raise.RaiseScope
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

/** An alias for [coerceToFailure]. */
@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.falter(
    transform: (Ok) -> Error,
): Failure<Error> = coerceToFailure(falter = transform)

/** An alias for [coerceToSuccess]. */
@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.recover(
    transform: (Error) -> Ok,
): Success<Ok> = coerceToSuccess(recover = transform)

/**
 * Returns a new [Success], after applying [recover] to the [Failure] error.
 *
 * - Transforms `Outcome<Ok, Error>` into `Success<Ok>`.
 * - If the receiver [Outcome] is an [Success], nothing happens.
 * - This function **does not** provide a [RaiseScope], and ***makes no guarantees*** about catching,
 *   handling, or rethrowing errors! Use [outcomeOf] within the transformation lambda for that.
 *
 * @receiver The [Outcome]<[Ok], [Error]> to transform.
 * @return A new [Success]<[Ok]> with the transformed value.
 *
 * @param Ok The `Ok` type of [Success].
 * @param Error The `Error` type of the receiver [Outcome].
 *
 * @param recover The transform function to convert an [Error] value into an [Ok] value.
 *
 * @see Outcome.coerceToFailure
 * @see Outcome.recover
 */
@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.coerceToSuccess(
    recover: (Error) -> Ok,
): Success<Ok> {
    contract { callsInPlace(recover, AT_MOST_ONCE) }
    return fold(
        failure = { Success(value = recover(error)) },
        success = Success<Ok>::caller, 
    )
}

/**
 * Returns a new [Failure], after applying [falter] to the [Success] value.
 *
 * - Transforms `Outcome<Ok, Error>` into `Failure<Ok, Error>`.
 * - If the receiver [Outcome] is an [Failure], nothing happens.
 * - This function **does not** provide a [RaiseScope], and ***makes no guarantees*** about catching,
 *   handling, or rethrowing errors! Use [outcomeOf] within the transformation lambda for that.
 *
 * @receiver The [Outcome]<[Ok], [Error]> to transform.
 * @return A new [Failure]<[Nothing], [Error]> with the transformed error.
 *
 * @param Ok The `Ok` type of the receiver [Outcome].
 * @param Error The `Error` type of [Failure].
 *
 * @param falter The transform function to convert an [Ok] value into an [Error] value.
 *
 * @see Outcome.coerceToSuccess
 * @see Outcome.falter
 */
@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.coerceToFailure(
    falter: (Ok) -> Error,
): Failure<Error> {
    contract { callsInPlace(falter, AT_MOST_ONCE) }

    return fold(
        success = { Failure(error = falter(value)) },
        failure = Failure<Error>::caller,
    )
}
