package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.inline.nulls
import nz.adjmunro.outcome.raise.RaiseScope
import nz.adjmunro.outcome.throwable.asThrowable
import kotlin.contracts.contract

/**
 * Returns the [Success][nz.adjmunro.outcome.Success] value, or [default] if the outcome is a
 * [Failure][nz.adjmunro.outcome.Failure].
 */
public infix fun <Ok, Error> Outcome<Ok, Error>.getOrDefault(default: Ok): Ok {
    return fold(success = Success<Ok>::value, failure = { default })
}

/**
 * Returns the [Success][nz.adjmunro.outcome.Success] value, or the result of [recover] applied to the
 * [Failure][nz.adjmunro.outcome.Failure] error.
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.getOrElse(recover: (Error) -> Ok): Ok {
    return fold(success = Success<Ok>::value, failure = { recover(error) })
}

/**
 * Returns the [Success][nz.adjmunro.outcome.Success] value, or `null` if the outcome is a
 * [Failure][nz.adjmunro.outcome.Failure].
 *
 * **Note:** `null` is ambiguous when `Ok` is itself a nullable type.
 * Use [getOrDefault][nz.adjmunro.outcome.members.getOrDefault] or
 * [getOrElse][nz.adjmunro.outcome.members.getOrElse] in that case.
 */
public fun <Ok, Error> Outcome<Ok, Error>.getOrNull(): Ok? {
    contract {
        returnsNotNull() implies (this@getOrNull is Success<Ok>)
        returns(value = null) implies (this@getOrNull is Failure<Error>)
    }

    return fold(success = Success<Ok>::value, failure = ::nulls)
}

/**
 * Returns the [Success][nz.adjmunro.outcome.Success] value, or throws if the outcome is a
 * [Failure][nz.adjmunro.outcome.Failure].
 *
 * @throws IllegalStateException if the receiver is a [Failure][nz.adjmunro.outcome.Failure].
 *
 * @see getOrRaise
 * @see getOrElse
 */
public fun <Ok, Error> Outcome<Ok, Error>.getOrThrow(): Ok {
    contract { returns() implies (this@getOrThrow is Success<Ok>) }

    return rfold(success = Success<Ok>::value) {
        throw error.asThrowable { "Outcome::getOrThrow threw! Got: $it" }
    }
}

/**
 * Returns the [Success][nz.adjmunro.outcome.Success] value, or short-circuits the surrounding
 * [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] with the
 * [Failure][nz.adjmunro.outcome.Failure] error.
 *
 * Equivalent to flattening from the inside: the inner error propagates outward through the scope.
 * This does **not** throw — it uses
 * [RaiseScope.shortCircuit][nz.adjmunro.outcome.raise.RaiseScope.shortCircuit] to unwind.
 *
 * @see getOrThrow
 * @see getOrElse
 */
context(scope: RaiseScope<OuterError>)
public fun <OuterError, Ok, InnerError: OuterError> Outcome<Ok, InnerError>.getOrRaise(): Ok {
    return getOrElse { e: InnerError -> scope.shortCircuit(error = e) }
}

/**
 * Returns the [Failure][nz.adjmunro.outcome.Failure] error, or [default] if the outcome is a
 * [Success][nz.adjmunro.outcome.Success].
 */
public infix fun <Ok, Error> Outcome<Ok, Error>.errorOrDefault(default: Error): Error {
    return fold(failure = Failure<Error>::error) { default }
}

/**
 * Returns the [Failure][nz.adjmunro.outcome.Failure] error, or the result of [faulter] applied to the
 * [Success][nz.adjmunro.outcome.Success] value.
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.errorOrElse(
    faulter: (Ok) -> Error,
): Error {
    return fold(success = { faulter(value) }, failure = Failure<Error>::error)
}

/**
 * Returns the [Failure][nz.adjmunro.outcome.Failure] error, or `null` if the outcome is a
 * [Success][nz.adjmunro.outcome.Success].
 *
 * **Note:** `null` is ambiguous when `Error` is itself a nullable type.
 * Use [errorOrDefault][nz.adjmunro.outcome.members.errorOrDefault] or
 * [errorOrElse][nz.adjmunro.outcome.members.errorOrElse] in that case.
 */
public fun <Ok, Error> Outcome<Ok, Error>.errorOrNull(): Error? {
    contract {
        returnsNotNull() implies (this@errorOrNull is Failure<Error>)
        returns(value = null) implies (this@errorOrNull is Success<Ok>)
    }

    return fold(success = ::nulls, failure = Failure<Error>::error)
}

/**
 * Returns the [Failure][nz.adjmunro.outcome.Failure] error, or throws if the outcome is a
 * [Success][nz.adjmunro.outcome.Success].
 *
 * @throws IllegalStateException if the receiver is a [Success][nz.adjmunro.outcome.Success].
 *
 * @see errorOrRaise
 * @see errorOrElse
 */
public fun <Ok, Error> Outcome<Ok, Error>.errorOrThrow(): Error {
    contract {
        returns() implies (this@errorOrThrow is Failure<Error>)
    }

    return fold(failure = Failure<Error>::error) {
        throw value.asThrowable { "Outcome::errorOrThrow threw! Got: $it" }
    }
}

/**
 * Returns the [Failure][nz.adjmunro.outcome.Failure] error, or short-circuits the surrounding
 * [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] with the
 * [Success][nz.adjmunro.outcome.Success] value.
 *
 * Equivalent to flattening **and swapping** from the inside: the inner success propagates outward as a raised error.
 * This does **not** throw — it uses
 * [RaiseScope.shortCircuit][nz.adjmunro.outcome.raise.RaiseScope.shortCircuit] to unwind.
 *
 * @see errorOrThrow
 * @see errorOrElse
 */
context(scope: RaiseScope<OuterError>)
public fun <OuterError, Ok: OuterError, InnerError> Outcome<Ok, InnerError>.errorOrRaise(): InnerError {
    return errorOrElse { ok: Ok -> scope.shortCircuit(error = ok) }
}
