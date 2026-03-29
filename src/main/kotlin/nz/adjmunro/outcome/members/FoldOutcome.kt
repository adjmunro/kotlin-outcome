package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Applies [success] or [failure] to the receiver [Outcome][nz.adjmunro.outcome.Outcome], returning [Output].
 *
 * - Unlike [map][nz.adjmunro.outcome.members.map], [Output] is unconstrained — it need not be an
 *   [Outcome][nz.adjmunro.outcome.Outcome].
 * - When [Output] is itself an [Outcome][nz.adjmunro.outcome.Outcome], `fold` doubles as a flatMap over both states.
 * - Use [collapse][nz.adjmunro.outcome.members.collapse] when the [Success][nz.adjmunro.outcome.Success] value and
 *   [Failure][nz.adjmunro.outcome.Failure] error share a common ancestor and no transformation is needed.
 *
 * @param success Lambda applied when the receiver is a [Success][nz.adjmunro.outcome.Success].
 * @param failure Lambda applied when the receiver is a [Failure][nz.adjmunro.outcome.Failure].
 *
 * @see rfold
 * @see collapse
 */
public inline fun <Ok, Error, Output> Outcome<Ok, Error>.fold(
    failure: Failure<Error>.() -> Output,
    success: Success<Ok>.() -> Output,
): Output {
    contract {
        callsInPlace(lambda = success, kind = InvocationKind.AT_MOST_ONCE)
        callsInPlace(lambda = failure, kind = InvocationKind.AT_MOST_ONCE)
    }

    return when (this@fold) {
        is Success<Ok> -> success()
        is Failure<Error> -> failure()
    }
}

/**
 * Reverse [fold][nz.adjmunro.outcome.members.fold] — identical behaviour with swapped lambda argument order.
 *
 * Useful when the trailing-lambda position reads more naturally as the [success] branch,
 * or when naming [failure] explicitly would be awkward.
 *
 * ```kotlin
 * val outcome: Outcome<String, Throwable> = ...
 *
 * throw outcome.rfold(failure = ::itself) { // it: Ok ->
 *     IllegalStateException("Expected a failure, got: $it")
 * }
 * ```
 *
 * @see fold
 * @see collapse
 */
public inline fun <Ok, Error, Output> Outcome<Ok, Error>.rfold(
    success: Success<Ok>.() -> Output,
    failure: Failure<Error>.() -> Output,
): Output {
    return fold(success = success, failure = failure)
}

/**
 * Extracts either the [Success][nz.adjmunro.outcome.Success] value or the
 * [Failure][nz.adjmunro.outcome.Failure] error, returning the nearest common [Ancestor] type.
 *
 * Use [fold][nz.adjmunro.outcome.members.fold] instead when each state needs a different transformation.
 *
 * @param Ancestor The nearest common ancestor of [Ok] and [Error].
 *
 * @see fold
 * @see rfold
 */
public fun <Ancestor, Ok : Ancestor, Error: Ancestor> Outcome<Ok, Error>.collapse(): Ancestor {
    return fold(success = Success<Ok>::value, failure = Failure<Error>::error)
}
