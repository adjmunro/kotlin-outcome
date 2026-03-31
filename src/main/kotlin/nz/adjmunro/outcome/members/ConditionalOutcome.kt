package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.throwable.asThrowable
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

/**
 * Throws if this is a [Failure][nz.adjmunro.outcome.Failure] **and** [predicate] returns `true` for the error.
 * Returns the original [Outcome][nz.adjmunro.outcome.Outcome] unchanged otherwise.
 *
 * @param fallbackMessage Produces the exception message from the error value. Only used when [Error] is not
 *   already a [Throwable] or [nz.adjmunro.outcome.throwable.ThrowableWrapper] — in those cases the
 *   existing exception is thrown as-is and this parameter is ignored.
 * @param predicate Returns `true` if the error should be thrown.
 * @throws Throwable The [Error] itself if it is a [Throwable], or its
 *   [cause][nz.adjmunro.outcome.throwable.ThrowableWrapper.cause] if it is a
 *   [ThrowableWrapper][nz.adjmunro.outcome.throwable.ThrowableWrapper], when [predicate] returns `true`.
 * @throws NullPointerException If the [Error] is `null` and [predicate] returns `true`.
 * @throws IllegalStateException If the [Error] is any other non-throwable type and [predicate] returns `true`.
 * @see nz.adjmunro.outcome.throwable.asThrowable
 */
public inline fun <Ok, Error> Outcome<Ok, Error>.throwIf(
    fallbackMessage: (Error) -> String = { "Outcome was Failure and throwIf predicate was true: $it" },
    predicate: (Error) -> Boolean
): Outcome<Ok, Error> {
    contract { callsInPlace(lambda = predicate, kind = InvocationKind.AT_MOST_ONCE) }

    if(isFailure() && predicate(error)) {
        throw error.asThrowable(fallbackMessage = fallbackMessage)
    }

    return this@throwIf
}

/**
 * Throws if this is a [Failure][nz.adjmunro.outcome.Failure] **and** [predicate] returns `false` for the error.
 * Returns the original [Outcome][nz.adjmunro.outcome.Outcome] unchanged otherwise.
 *
 * @param fallbackMessage Produces the exception message from the error value. Only used when [Error] is not
 *   already a [Throwable] or [nz.adjmunro.outcome.throwable.ThrowableWrapper] — in those cases the
 *   existing exception is thrown as-is and this parameter is ignored.
 * @param predicate Returns `false` if the error should be thrown.
 * @throws Throwable The [Error] itself if it is a [Throwable], or its
 *   [cause][nz.adjmunro.outcome.throwable.ThrowableWrapper.cause] if it is a
 *   [ThrowableWrapper][nz.adjmunro.outcome.throwable.ThrowableWrapper], when [predicate] returns `false`.
 * @throws NullPointerException If the [Error] is `null` and [predicate] returns `false`.
 * @throws IllegalStateException If the [Error] is any other non-throwable type and [predicate] returns `false`.
 * @see nz.adjmunro.outcome.throwable.asThrowable
 */
public inline fun <Ok, Error> Outcome<Ok, Error>.throwUnless(
    fallbackMessage: (Error) -> String = { "Outcome was Failure and throwUnless predicate was false: $it" },
    predicate: (Error) -> Boolean,
): Outcome<Ok, Error> {
    contract { callsInPlace(lambda = predicate, kind = InvocationKind.AT_MOST_ONCE) }

    if(isFailure() && !predicate(error)) {
        throw error.asThrowable(fallbackMessage = fallbackMessage)
    }

    return this@throwUnless
}
