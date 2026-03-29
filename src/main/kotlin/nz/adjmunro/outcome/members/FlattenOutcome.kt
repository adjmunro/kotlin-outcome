package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.inline.caller

/**
 * Property alias for [flattenNestedSuccess][nz.adjmunro.outcome.members.flattenNestedSuccess].
 *
 * Unwraps a nested [Outcome][nz.adjmunro.outcome.Outcome] from the
 * [Success][nz.adjmunro.outcome.Success] state. The returned error type is the nearest common
 * ancestor of both error types.
 *
 * @param Ok The [Success][nz.adjmunro.outcome.Success] [value][nz.adjmunro.outcome.Success.value] type.
 * @param Err The nearest common ancestor of [Eri] and [Ero]; the error type of the returned
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 * @param Eri The inner (nested) [Failure][nz.adjmunro.outcome.Failure] [error][nz.adjmunro.outcome.Failure.error] type.
 * @param Ero The outer [Failure][nz.adjmunro.outcome.Failure] [error][nz.adjmunro.outcome.Failure.error] type.
 *
 * @see flattenNestedSuccess
 * @see flattenNestedFailure
 * @see flattenNestedBoth
 */
@get:JvmName("flattenNestedSuccessAlias")
public val <Ok, Err, Eri, Ero> Outcome<Outcome<Ok, Eri>, Ero>.flatten: Outcome<Ok, Err> where
        Eri : Err, Ero : Err
    get() = flattenNestedSuccess()

/**
 * Property alias for [flattenNestedFailure][nz.adjmunro.outcome.members.flattenNestedFailure].
 *
 * Unwraps a nested [Outcome][nz.adjmunro.outcome.Outcome] from the
 * [Failure][nz.adjmunro.outcome.Failure] state. The returned value type is the nearest common
 * ancestor of both value types.
 *
 * @param Ok The nearest common ancestor of [Oki] and [Oko]; the value type of the returned
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 * @param Err The [Failure][nz.adjmunro.outcome.Failure] [error][nz.adjmunro.outcome.Failure.error] type.
 * @param Oki The inner (nested) [Success][nz.adjmunro.outcome.Success] [value][nz.adjmunro.outcome.Success.value] type.
 * @param Oko The outer [Success][nz.adjmunro.outcome.Success] [value][nz.adjmunro.outcome.Success.value] type.
 *
 * @see flattenNestedSuccess
 * @see flattenNestedFailure
 * @see flattenNestedBoth
 */
@get:JvmName("flattenNestedFailureAlias")
public val <Ok, Err, Oki, Oko> Outcome<Oko, Outcome<Oki, Err>>.flatten: Outcome<Ok, Err> where
        Oko : Ok, Oki : Ok
    get() = flattenNestedFailure()

/**
 * Property alias for [flattenNestedBoth][nz.adjmunro.outcome.members.flattenNestedBoth].
 *
 * Unwraps nested [Outcome][nz.adjmunro.outcome.Outcome] values from **both** the
 * [Success][nz.adjmunro.outcome.Success] and [Failure][nz.adjmunro.outcome.Failure] states.
 * The returned types are the nearest common ancestors across both branches.
 *
 * @param Ok The nearest common ancestor of [Oks] and [Okf]; the value type of the returned
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 * @param Err The nearest common ancestor of [Ers] and [Erf]; the error type of the returned
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 * @param Oks The [Success][nz.adjmunro.outcome.Success] [value][nz.adjmunro.outcome.Success.value] type
 *   nested inside the [Success][nz.adjmunro.outcome.Success] state.
 * @param Ers The [Failure][nz.adjmunro.outcome.Failure] [error][nz.adjmunro.outcome.Failure.error] type
 *   nested inside the [Success][nz.adjmunro.outcome.Success] state.
 * @param Okf The [Success][nz.adjmunro.outcome.Success] [value][nz.adjmunro.outcome.Success.value] type
 *   nested inside the [Failure][nz.adjmunro.outcome.Failure] state.
 * @param Erf The [Failure][nz.adjmunro.outcome.Failure] [error][nz.adjmunro.outcome.Failure.error] type
 *   nested inside the [Failure][nz.adjmunro.outcome.Failure] state.
 *
 * @see flattenNestedSuccess
 * @see flattenNestedFailure
 * @see flattenNestedBoth
 */
@get:JvmName("flattenNestedBothAlias")
public val <Ok, Err, Oks, Ers, Okf, Erf> Outcome<Outcome<Oks, Ers>, Outcome<Okf, Erf>>.flatten: Outcome<Ok, Err> where
        Oks : Ok, Ers : Err, Okf : Ok, Erf : Err
    get() = flattenNestedBoth()

/**
 * Flattens a nested [Outcome][nz.adjmunro.outcome.Outcome] from inside the
 * [Success][nz.adjmunro.outcome.Success] state into a single
 * [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * The returned error type is the nearest common [AncestorError] of [EmbeddedError] and [OuterError].
 *
 * @param Ok The [Success][nz.adjmunro.outcome.Success] [value][nz.adjmunro.outcome.Success.value] type.
 * @param EmbeddedError The [Failure][nz.adjmunro.outcome.Failure] error type nested inside the
 *   [Success][nz.adjmunro.outcome.Success] state.
 * @param OuterError The [Failure][nz.adjmunro.outcome.Failure] error type of the outer
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 * @param AncestorError The nearest common ancestor of [EmbeddedError] and [OuterError].
 *
 * @see flatten
 * @see flattenNestedFailure
 * @see flattenNestedBoth
 */
public fun <Ok, EmbeddedError, OuterError, AncestorError> Outcome<Outcome<Ok, EmbeddedError>, OuterError>.flattenNestedSuccess(): Outcome<Ok, AncestorError> where
        EmbeddedError : AncestorError,
        OuterError : AncestorError
{
    return fold(
        failure = Failure<OuterError>::caller,
        success = Success<Outcome<Ok, EmbeddedError>>::value,
    )
}

/**
 * Flattens a nested [Outcome][nz.adjmunro.outcome.Outcome] from inside the
 * [Failure][nz.adjmunro.outcome.Failure] state into a single
 * [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * The returned value type is the nearest common [AncestorOk] of [EmbeddedOk] and [OuterOk].
 *
 * @param Error The [Failure][nz.adjmunro.outcome.Failure] [error][nz.adjmunro.outcome.Failure.error] type.
 * @param OuterOk The [Success][nz.adjmunro.outcome.Success] value type of the outer
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 * @param EmbeddedOk The [Success][nz.adjmunro.outcome.Success] value type nested inside the
 *   [Failure][nz.adjmunro.outcome.Failure] state.
 * @param AncestorOk The nearest common ancestor of [EmbeddedOk] and [OuterOk].
 *
 * @see flatten
 * @see flattenNestedSuccess
 * @see flattenNestedBoth
 */
public fun <OuterOk, EmbeddedOk, Error, AncestorOk> Outcome<OuterOk, Outcome<EmbeddedOk, Error>>.flattenNestedFailure(): Outcome<AncestorOk, Error> where
        OuterOk : AncestorOk,
        EmbeddedOk : AncestorOk
{
    return fold(
        failure = Failure<Outcome<EmbeddedOk, Error>>::error,
        success = Success<OuterOk>::caller,
    )
}

/**
 * Flattens nested [Outcome][nz.adjmunro.outcome.Outcome] values from **both** the
 * [Success][nz.adjmunro.outcome.Success] and [Failure][nz.adjmunro.outcome.Failure] states into
 * a single [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * The returned value and error types are the nearest common [AncestorOk] and [AncestorError]
 * across both branches.
 *
 * @param SuccessOk The [Success][nz.adjmunro.outcome.Success] value type nested inside the
 *   [Success][nz.adjmunro.outcome.Success] state.
 * @param SuccessError The [Failure][nz.adjmunro.outcome.Failure] error type nested inside the
 *   [Success][nz.adjmunro.outcome.Success] state.
 * @param FailureOk The [Success][nz.adjmunro.outcome.Success] value type nested inside the
 *   [Failure][nz.adjmunro.outcome.Failure] state.
 * @param FailureError The [Failure][nz.adjmunro.outcome.Failure] error type nested inside the
 *   [Failure][nz.adjmunro.outcome.Failure] state.
 * @param AncestorOk The nearest common ancestor of [SuccessOk] and [FailureOk].
 * @param AncestorError The nearest common ancestor of [SuccessError] and [FailureError].
 *
 * @see flatten
 * @see flattenNestedSuccess
 * @see flattenNestedFailure
 */
public fun <SuccessOk, SuccessError, FailureOk, FailureError, AncestorOk, AncestorError> Outcome<Outcome<SuccessOk, SuccessError>, Outcome<FailureOk, FailureError>>.flattenNestedBoth(): Outcome<AncestorOk, AncestorError> where
        SuccessOk : AncestorOk,
        SuccessError : AncestorError,
        FailureOk : AncestorOk,
        FailureError : AncestorError
{
    return fold(
        failure = Failure<Outcome<FailureOk, FailureError>>::error,
        success = Success<Outcome<SuccessOk, SuccessError>>::value,
    )
}
