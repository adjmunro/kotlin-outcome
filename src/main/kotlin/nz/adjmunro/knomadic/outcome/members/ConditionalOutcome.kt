package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

@OutcomeDsl
public fun <Ok : Any, Error : Any> Outcome<Ok, Error>.isSuccess(): Boolean {
    contract {
        returns(true) implies (this@isSuccess is Success<Ok>)
        returns(false) implies (this@isSuccess is Failure<Error>)
    }

    return this@isSuccess is Success<Ok>
}

@OutcomeDsl
public fun <Ok : Any, Error : Any> Outcome<Ok, Error>.isFailure(): Boolean {
    contract {
        returns(true) implies (this@isFailure is Failure<Error>)
        returns(false) implies (this@isFailure is Success<Ok>)
    }

    return this@isFailure is Failure<Error>
}

@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.isSuccess(
    predicate: (Ok) -> Boolean,
): Boolean {
    contract {
        returns(true) implies (this@isSuccess is Success<Ok>)
        returns(false) implies (this@isSuccess is Failure<Error>)
        callsInPlace(predicate, AT_MOST_ONCE)
    }

    return isSuccess() && predicate(value)
}

@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.isFailure(
    predicate: (Error) -> Boolean,
): Boolean {
    contract {
        returns(true) implies (this@isFailure is Failure<Error>)
        returns(false) implies (this@isFailure is Success<Ok>)
        callsInPlace(predicate, AT_MOST_ONCE)
    }

    return isFailure() && predicate(error)
}

@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.onSuccess(
    block: (Ok) -> Unit,
): Outcome<Ok, Error> {
    contract { callsInPlace(block, AT_MOST_ONCE) }
    if (isSuccess()) block(value)
    return this@onSuccess
}

@OutcomeDsl
public inline infix fun <Ok : Any, Error : Any> Outcome<Ok, Error>.onFailure(
    block: (Error) -> Unit,
): Outcome<Ok, Error> {
    contract { callsInPlace(block, AT_MOST_ONCE) }
    if (isFailure()) block(error)
    return this@onFailure
}
