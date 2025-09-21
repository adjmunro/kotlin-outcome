package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.inline.caller
import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success

/**
 * An overloaded alias for [Outcome.flattenNestedSuccess].
 *
 * @param Ok The [value][Success.value] type.
 * @param Err The ancestor of [Eri] & [Ero].
 * @param Eri The inner [error][Failure.error] type.
 * @param Ero The outer [error][Failure.error] type.
 *
 * @see Outcome.flattenNestedSuccess
 * @see Outcome.flattenNestedFailure
 * @see Outcome.flattenNestedBoth
 */
@OutcomeDsl
@get:JvmName("flattenNestedSuccessAlias")
public val <Ok, Err, Eri, Ero> Outcome<Outcome<Ok, Eri>, Ero>.flatten: Outcome<Ok, Err> where
        Ok : Any, Err : Any, Eri : Err, Ero : Err
    get() = flattenNestedSuccess()

/**
 * An overloaded alias for [Outcome.flattenNestedFailure].
 *
 * @param Ok The ancestor of [Oki] & [Oko].
 * @param Err The [error][Failure.error] type.
 * @param Oki The inner [value][Success.value] type.
 * @param Oko The outer [value][Success.value] type.
 *
 * @see Outcome.flattenNestedSuccess
 * @see Outcome.flattenNestedFailure
 * @see Outcome.flattenNestedBoth
 */
@OutcomeDsl
@get:JvmName("flattenNestedFailureAlias")
public val <Ok, Err, Oki, Oko> Outcome<Oko, Outcome<Oki, Err>>.flatten: Outcome<Ok, Err> where
        Ok : Any, Err : Any, Oko : Ok, Oki : Ok
    get() = flattenNestedFailure()

/**
 * An overloaded alias for [Outcome.flattenNestedBoth].
 *
 * @param Ok The ancestor of [Oks] & [Okf].
 * @param Err The ancestor of [Ers] & [Erf].
 * @param Oks The success [value][Success.value] type.
 * @param Ers The success [error][Failure.error] type.
 * @param Okf The failure [value][Success.value] type.
 * @param Erf The failure [error][Failure.error] type.
 *
 * @see Outcome.flattenNestedSuccess
 * @see Outcome.flattenNestedFailure
 * @see Outcome.flattenNestedBoth
 */
@OutcomeDsl
@get:JvmName("flattenNestedBothAlias")
public val <Ok, Err, Oks, Ers, Okf, Erf> Outcome<Outcome<Oks, Ers>, Outcome<Okf, Erf>>.flatten: Outcome<Ok, Err> where
        Ok : Any, Err : Any, Oks : Ok, Ers : Err, Okf : Ok, Erf : Err
    get() = flattenNestedBoth()

/**
 * Flatten a nested [Outcome] inside the [Success] state, into a single [Outcome].
 *
 * *The `Error` type of the returned [Outcome] will be the nearest common [AncestorError] of
 * [EmbeddedError] and [OuterError].*
 *
 * @receiver The [Success] of an [Outcome]<[Ok], [EmbeddedError]> to flatten.
 * @return The flattened [Outcome]<[Ok], [AncestorError]>.
 *
 * @param Ok The type of the [Success] value.
 * @param EmbeddedError The type of the [Failure] error nested inside the [Success].
 * @param OuterError The type of the non-nested [Failure] error.
 * @param AncestorError The nearest common ancestor type of [EmbeddedError] and [OuterError].
 *
 * @see Outcome.flatten
 * @see Outcome.flattenNestedFailure
 * @see Outcome.flattenNestedBoth
 */
@OutcomeDsl
public fun <Ok, EmbeddedError, OuterError, AncestorError> Outcome<Outcome<Ok, EmbeddedError>, OuterError>.flattenNestedSuccess(): Outcome<Ok, AncestorError> where
        Ok : Any,
        AncestorError : Any,
        EmbeddedError : AncestorError,
        OuterError : AncestorError
{
    return fold(
        failure = Failure<OuterError>::caller,
        success = Success<Outcome<Ok, EmbeddedError>>::value,
    )
}

/**
 * Flatten a nested [Outcome] inside the [Failure] state, into a single [Outcome].
 *
 * *The `Ok` type of the returned [Outcome] will be the nearest common [AncestorOk] of
 * [EmbeddedOk] and [OuterOk].*
 *
 * @receiver The [Failure] of an [Outcome]<[EmbeddedOk], [Error]> to flatten.
 * @return The flattened [Outcome]<[AncestorOk], [Error]>.
 *
 * @param Error The type of the [Failure] error.
 * @param OuterOk The type of the non-nested [Success] value.
 * @param EmbeddedOk The type of the [Success] value nested inside the [Failure].
 * @param AncestorOk The nearest common ancestor type of [EmbeddedOk] and [OuterOk].
 *
 * @see Outcome.flatten
 * @see Outcome.flattenNestedSuccess
 * @see Outcome.flattenNestedBoth
 */
@OutcomeDsl
public fun <OuterOk, EmbeddedOk, Error, AncestorOk> Outcome<OuterOk, Outcome<EmbeddedOk, Error>>.flattenNestedFailure(): Outcome<AncestorOk, Error> where
        AncestorOk : Any,
        Error : Any,
        OuterOk : AncestorOk,
        EmbeddedOk : AncestorOk
{
    return fold(
        failure = Failure<Outcome<EmbeddedOk, Error>>::error,
        success = Success<OuterOk>::caller,
    )
}

/**
 * Flatten the nested [Outcome] inside both [Success] and [Failure] states, into a single [Outcome].
 *
 * *The `Ok` and `Error` types of the returned [Outcome] will be the nearest common [AncestorOk] and [AncestorError].*
 *
 * @receiver The [Outcome] to flatten, with a nested [Outcome] inside both success and failure states.
 * @return The flattened [Outcome]<[AncestorOk], [AncestorError]>.
 *
 * @param SuccessOk The type of the [Success] value nested inside the [Success].
 * @param SuccessError The type of the [Failure] error nested inside the [Success].
 * @param FailureOk The type of the [Success] value nested inside the [Failure].
 * @param FailureError The type of the [Failure] error nested inside the [Failure].
 * @param AncestorOk The nearest common ancestor type of [SuccessOk] and [FailureOk].
 * @param AncestorError The nearest common ancestor type of [SuccessError] and [FailureError].
 *
 * @see Outcome.flatten
 * @see Outcome.flattenNestedSuccess
 * @see Outcome.flattenNestedFailure
 */
@OutcomeDsl
public fun <SuccessOk, SuccessError, FailureOk, FailureError, AncestorOk, AncestorError> Outcome<Outcome<SuccessOk, SuccessError>, Outcome<FailureOk, FailureError>>.flattenNestedBoth(): Outcome<AncestorOk, AncestorError> where
        AncestorOk : Any,
        AncestorError : Any,
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
