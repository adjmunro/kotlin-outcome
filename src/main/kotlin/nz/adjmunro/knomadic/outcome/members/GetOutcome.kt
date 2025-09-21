package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.inline.nulls
import nz.adjmunro.inline.rethrow
import nz.adjmunro.inline.throwfold
import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success
import kotlin.contracts.contract

/** @return The [value][Success] or [default]. */
@OutcomeDsl
public infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.getOrDefault(default: Ok): Ok {
    return fold(success = Success<Ok>::value, failure = { default })
}

/** @return The [value][Success] or the result of [recover]. */
@OutcomeDsl
public inline infix fun <Ok, Error : Any> Outcome<Ok & Any, Error>.getOrElse(
    recover: (Error) -> Ok,
): Ok {
    return fold(success = Success<Ok & Any>::value, failure = { recover(error) })
}

/** @return The [value][Success] or `null`. */
@OutcomeDsl
public fun <Ok : Any, Error : Any> Outcome<Ok, Error>.getOrNull(): Ok? {
    contract {
        returnsNotNull() implies (this@getOrNull is Success<Ok>)
        returns(null) implies (this@getOrNull is Failure<Error>)
    }

    return fold(success = Success<Ok>::value, failure = ::nulls)
}

/**
 * @return The [value][Success] or `throws`.
 * @throws IllegalStateException if the [Outcome] is a [failure][Failure].
 */
@OutcomeDsl
public fun <Ok : Any, Error : Any> Outcome<Ok, Error>.getOrThrow(): Ok {
    contract { returns() implies (this@getOrThrow is Success<Ok>) }

    return rfold(success = Success<Ok>::value) {
        error.throwfold(throws = ::rethrow) { error("Outcome::getOrThrow threw! Got: $this") }
    }
}

/** @return The [error][Failure] or [default]. */
@OutcomeDsl
public infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.errorOrDefault(default: Error): Error {
    return fold(failure = Failure<Error>::error) { default }
}

/** @return The [error][Failure] or the result of [faulter]. */
@OutcomeDsl
public inline infix fun <Ok : Any, Error> Outcome<Ok, Error & Any>.errorOrElse(
    faulter: (Ok) -> Error,
): Error {
    return fold(success = { faulter(value) }, failure = Failure<Error & Any>::error)
}

/** @return The [error][Failure] or `null`. */
@OutcomeDsl
public fun <Ok : Any, Error : Any> Outcome<Ok, Error>.errorOrNull(): Error? {
    contract {
        returnsNotNull() implies (this@errorOrNull is Failure<Error>)
        returns(null) implies (this@errorOrNull is Success<Ok>)
    }

    return fold(success = ::nulls, failure = Failure<Error>::error)
}

/**
 * @return The [error][Failure] or `throws`.
 * @throws IllegalStateException if the [Outcome] is a [success][Success].
 */
@OutcomeDsl
public fun <Ok : Any, Error : Any> Outcome<Ok, Error>.errorOrThrow(): Error {
    contract {
        returns() implies (this@errorOrThrow is Failure<Error>)
    }

    return fold(failure = Failure<Error>::error) {
        value.throwfold(throws = ::rethrow) { error("Outcome::errorOrThrow threw! Got: $this") }
    }
}
