package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/** Returns `true` if this [Outcome][nz.adjmunro.outcome.Outcome] is a [Success][nz.adjmunro.outcome.Success]. */
public fun <Ok, Error> Outcome<Ok, Error>.isSuccess(): Boolean {
    contract {
        returns(value = true).implies(booleanExpression = this@isSuccess is Success<Ok>)
        returns(value = false).implies(booleanExpression = this@isSuccess is Failure<Error>)
    }

    return this@isSuccess is Success<Ok>
}

/** Returns `true` if this [Outcome][nz.adjmunro.outcome.Outcome] is a [Failure][nz.adjmunro.outcome.Failure]. */
public fun <Ok, Error> Outcome<Ok, Error>.isFailure(): Boolean {
    contract {
        returns(value = true).implies(booleanExpression = this@isFailure is Failure<Error>)
        returns(value = false).implies(booleanExpression = this@isFailure is Success<Ok>)
    }

    return this@isFailure is Failure<Error>
}

/**
 * Returns `true` if this is a [Success][nz.adjmunro.outcome.Success] **and** [predicate] returns `true` for the value.
 *
 * Returns `false` if this is a [Failure][nz.adjmunro.outcome.Failure], or if [predicate] returns `false`.
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.isSuccess(
    predicate: (Ok) -> Boolean,
): Boolean {
    contract {
        returns(value = true).implies(booleanExpression = this@isSuccess is Success<Ok>)
        callsInPlace(lambda = predicate, kind = InvocationKind.AT_MOST_ONCE)
    }

    return isSuccess() && predicate(value)
}

/**
 * Returns `true` if this is a [Failure][nz.adjmunro.outcome.Failure] **and** [predicate] returns `true` for the error.
 *
 * Returns `false` if this is a [Success][nz.adjmunro.outcome.Success], or if [predicate] returns `false`.
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.isFailure(
    predicate: (Error) -> Boolean,
): Boolean {
    contract {
        returns(value = true).implies(booleanExpression = this@isFailure is Failure<Error>)
        callsInPlace(lambda = predicate, kind = InvocationKind.AT_MOST_ONCE)
    }

    return isFailure() && predicate(error)
}

/**
 * Calls [block] with the [Success][nz.adjmunro.outcome.Success] value if this is a success, then returns the original
 * [Outcome][nz.adjmunro.outcome.Outcome] unchanged. No-op on [Failure][nz.adjmunro.outcome.Failure].
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.onSuccess(
    block: (Ok) -> Unit,
): Outcome<Ok, Error> {
    contract { callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE) }
    if (isSuccess()) block(value)
    return this@onSuccess
}

/**
 * Calls [block] with the [Failure][nz.adjmunro.outcome.Failure] error if this is a failure, then returns the original
 * [Outcome][nz.adjmunro.outcome.Outcome] unchanged. No-op on [Success][nz.adjmunro.outcome.Success].
 */
public inline infix fun <Ok, Error> Outcome<Ok, Error>.onFailure(
    block: (Error) -> Unit,
): Outcome<Ok, Error> {
    contract { callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE) }
    if (isFailure()) block(error)
    return this@onFailure
}
