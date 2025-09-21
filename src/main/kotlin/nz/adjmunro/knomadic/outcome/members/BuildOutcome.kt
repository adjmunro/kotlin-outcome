@file:Suppress("NOTHING_TO_INLINE")

package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.inline.itself
import nz.adjmunro.inline.nulls
import nz.adjmunro.inline.rethrow
import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Faulty
import nz.adjmunro.knomadic.outcome.Maybe
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success
import nz.adjmunro.knomadic.raise.RaiseScope
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.catch
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.default
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.fold
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.raise
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * An inline hack to produce a [Success] outcome with no [value][Success.value].
 *
 * *Usually used when working with [Faulty].*
 */
@OutcomeDsl
public inline fun emptySuccess(ignore: Any? = null): Success<Unit> = Success(value = Unit)

/**
 * An inline hack to produce a [Failure] outcome with no [error][Failure.error].
 *
 * *Usually used when working with [Maybe].*
 */
@OutcomeDsl
public inline fun emptyFailure(ignore: Any? = null): Failure<Unit> = Failure(error = Unit)

/**
 * Builds a [Success] outcome with the value provided by the [block].
 *
 * @param Ok The output type of the [block], which must be a subtype of [Any].
 * @param block The block that provides the value for the success outcome.
 * @return A [Success] instance containing the value returned by the block.
 */
@OutcomeDsl
public inline fun <Ok : Any> successOf(block: () -> Ok): Success<Ok> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return Success(value = block())
}

/**
 * Builds a [Success] outcome with the value provided by the [block].
 *
 * @receiver Some input type, [In], passed to [block].
 * @param Out The output type of the [block], which must be a subtype of [Any].
 * @param block The block that provides the value for the success outcome.
 * @return A [Success] instance containing the value returned by the block.
 */
@OutcomeDsl
public inline fun <In, Out : Any> In.successOf(block: (In) -> Out): Success<Out> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return Success(value = block(this))
}

/**
 * Builds a [Failure] outcome with the error provided by the [block].
 *
 * @param Error The error type of the [Failure], which must be a subtype of [Any].
 * @param block The block that provides the error for the failure outcome.
 * @return A [Failure] instance containing the error returned by the block.
 */
@OutcomeDsl
public inline fun <Error : Any> failureOf(block: () -> Error): Failure<Error> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return Failure(error = block())
}

/**
 * Builds a [Failure] outcome with the error provided by the [block].
 *
 * @receiver Some input type, [In], passed to [block].
 * @param Out The output type of the [block], which must be a subtype of [Any].
 * @param block The block that provides the error for the failure outcome.
 * @return A [Failure] instance containing the error returned by the block.
 */
@OutcomeDsl
public inline fun <In, Out : Any, Error : Any> In.failureOf(block: (In) -> Error): Failure<Error> {
    contract { callsInPlace(block, InvocationKind.EXACTLY_ONCE) }
    return Failure(error = block(this))
}

/**
 * An alias for [outcomeOf] that uses [Throwable] as the [Error] type.
 *
 * > Useful for cases where you want to catch & wrap all exceptions to
 * > handle them as [Failure].
 *
 * @param Ok The output type of the [block], which must be a subtype of [Any].
 * @param block The block that provides the value for the success outcome.
 * @return An [Outcome] instance containing the result of the block execution.
 * @see outcomeOf
 * @see outcome
 */
@OutcomeDsl
public inline fun <Ok : Any> catch(
    block: RaiseScope<Throwable>.() -> Ok,
): Outcome<Ok, Throwable> = outcomeOf(catch = ::Failure, block = block)

/**
 * An alias for [outcomeOf] that uses [Throwable] as the [Error] type.
 *
 * > Useful for cases where you want to catch & wrap all exceptions to
 * > handle them as [Failure].
 *
 * @receiver Some input type, [In], passed to [block].
 * @param Out The output type of the [block], which must be a subtype of [Any].
 * @param block The block that provides the value for the success outcome.
 * @return An [Outcome] instance containing the result of the block execution.
 * @see outcomeOf
 * @see outcome
 */
@OutcomeDsl
public inline fun <In, Out : Any> In.catch(
    block: RaiseScope<Throwable>.(In) -> Out,
): Outcome<Out, Throwable> = outcomeOf(catch = ::Failure, block = block)

/**
 * An alias for [outcomeOf] that uses a [String] as the [Error] type.
 *
 * > Useful for simple cases, where you fail to provide a specific error type,
 * > and just want to use any message string or [Throwable.message].
 *
 * @param Ok The output type of the [block], which must be a subtype of [Any].
 * @param block The block that provides the value for the success outcome.
 * @return An [Outcome] instance containing the result of the block execution.
 * @see outcomeOf
 * @see catch
 */
@OutcomeDsl
public inline fun <Ok : Any> outcome(
    block: RaiseScope<String>.() -> Ok,
): Outcome<Ok, String> = outcomeOf(
    catch = { e: Throwable -> Failure(error = e.message ?: e.toString()) },
    block = block
)

/**
 * An alias for [outcomeOf] that uses a [String] as the [Error] type.
 *
 * > Useful for simple cases, where you fail to provide a specific error type,
 * > and just want to use any message string or [Throwable.message].
 *
 * @receiver Some input type, [In], passed to [block].
 * @param Out The output type of the [block], which must be a subtype of [Any].
 * @param block The block that provides the value for the success outcome.
 * @return An [Outcome] instance containing the result of the block execution.
 * @see outcomeOf
 * @see catch
 */
@OutcomeDsl
public inline fun <In, Out : Any> In.outcome(
    block: RaiseScope<String>.(In) -> Out,
): Outcome<Out, String> = outcomeOf(
    catch = { e: Throwable -> Failure(error = e.message ?: e.toString()) },
    block = block
)

/**
 * Context runner that encapsulates the [Ok] result of [block] as an [Success], and any
 * [raised][RaiseScope.raise] or [caught][RaiseScope.catch] [errors][Error] as an [Failure].
 *
 * > ***Note:** [catch] will [rethrow] by default. This is because the consumer needs to manually
 * > override the parameter and map it to an [Outcome] (if desired). Assigning it to
 * > [Failure][Failure] directly will only force [Error] to be interpreted as [Throwable]
 * > by the [RaiseScope], which may interfere with the intended [Error] type!*
 * ```kotlin
 * // Outcome<Unit, Throwable>
 * outcomeOf(::Failure) { // this: RaiseScope<Throwable> -> ... }
 *
 * // Outcome<Int, String>
 * outcomeOf { // this: RaiseScope<String> ->
 *     raise { "error" }
 *     return 3
 * }
 *
 * // Outcome<String, NullPointerException>
 * outcomeOf { // this: RaiseScope<NullPointerException> ->
 *     catch({ it }) { throw NullPointerException() }
 * }
 * ```
 *
 * @param catch Map thrown exceptions to an [Outcome]. (Throws by default).
 * @param block The code to execute.
 * @see outcome
 * @see catch
 */
@OutcomeDsl
public inline fun <Ok : Any, Error : Any> outcomeOf(
    catch: (throwable: Throwable) -> Outcome<Ok, Error> = ::rethrow,
    @BuilderInference block: RaiseScope<Error>.() -> Ok,
): Outcome<Ok, Error> = RaiseScope.default {
    fold(block = block, catch = catch, recover = ::Failure, transform = ::Success)
}

/**
 * Context runner that encapsulates the result of [block] as an [Success], and any
 * [raised][RaiseScope.raise] or [caught][RaiseScope.catch] [errors][Error] as an [Failure].
 *
 * > ***Note:** [catch] will [rethrow] by default. This is because the consumer needs to manually
 * > override the parameter and map it to an [Outcome] (if desired). Assigning it to
 * > [Failure][Failure] directly will only force [Error] to be interpreted as [Throwable]
 * > by the [RaiseScope], which may interfere with the intended [Error] type!*
 * ```kotlin
 * // Outcome<Unit, Throwable>
 * outcomeOf(::Failure) { // this: RaiseScope<Throwable> -> ... }
 *
 * // Outcome<Int, String>
 * 3.outcomeOf { // this: RaiseScope<String>, it: Int ->
 *     raise { "error" }
 *     return it
 * }
 *
 * // Outcome<String, NullPointerException>
 * outcomeOf { // this: RaiseScope<NullPointerException> ->
 *     catch({ it }) { throw NullPointerException() }
 * }
 * ```
 *
 * @receiver Some input type, [In], passed to [block].
 * @param Out The output type of the [block], which must be a subtype of [Any].
 * @param Error The error type of the [Outcome], which must be a subtype of [Any].
 * @param catch Map thrown exceptions to an [Outcome]. (Throws by default).
 * @param block The code to execute.
 * @see outcome
 * @see catch
 */
@OutcomeDsl
public inline fun <In, Out: Any, Error : Any> In.outcomeOf(
    catch: (throwable: Throwable) -> Outcome<Out, Error> = ::rethrow,
    @BuilderInference block: RaiseScope<Error>.(In) -> Out,
): Outcome<Out, Error> = RaiseScope.default {
    fold(
        block = { block(this@outcomeOf) },
        catch = catch,
        recover = ::Failure,
        transform = ::Success,
    )
}

/**
 * Context runner that encapsulates the result of [block] as a [Maybe].
 *
 * > ***Note:** [catch] will return an [emptyFailure] by default.*
 * ```kotlin
 * // Maybe<Int>
 * maybeOf { 3 }
 *
 * // Maybe<String>
 * maybeOf { "value" }
 * ```
 *
 * @param catch Map thrown exceptions to some [Maybe]. (Returns an [emptyFailure] by default).
 * @param block The code to execute.
 * @see maybeOf
 */
@OutcomeDsl
public inline fun <Ok : Any> maybeOf(
    @BuilderInference catch: (throwable: Throwable) -> Maybe<Ok> = ::emptyFailure,
    block: RaiseScope<Unit>.() -> Ok,
): Maybe<Ok> = outcomeOf(catch = catch, block = block)

/**
 * Context runner that encapsulates the result of [block] as a [Maybe].
 *
 * > ***Note:** [catch] will return an [emptyFailure] by default.*
 * ```kotlin
 * // Maybe<Int>
 * 3.maybeOf { it + 1 }
 *
 * // Maybe<String>
 * "value".maybeOf { it.uppercase() }
 * ```
 *
 * @receiver Some input type, [In], passed to [block].
 * @param Out The output type of the [block], which must be a subtype of [Any].
 * @param catch Map thrown exceptions to some [Maybe]. (Returns an [emptyFailure] by default).
 * @param block The code to execute.
 * @see maybeOf
 */
@OutcomeDsl
public inline fun <In, Out : Any> In.maybeOf(
    @BuilderInference catch: (throwable: Throwable) -> Maybe<Out> = ::rethrow,
    block: RaiseScope<Unit>.(In) -> Out,
): Maybe<Out> = outcomeOf(catch = catch, block = block)

/**
 * Context runner that encapsulates the result of [block] as a [Faulty].
 *
 * > ***Note:** [catch] will [rethrow] by default.*
 * ```kotlin
 * // Faulty<String>
 * faultyOf { "error" }
 *
 * // Faulty<Throwable>
 * faultyOf { throw IllegalStateException("error") }
 * ```
 *
 * @param catch Map thrown exceptions to a [Faulty]. (Throws by default).
 * @param block The code to execute.
 * @see faultyOf
 */
@OutcomeDsl
public inline fun <Error : Any> faultyOf(
    @BuilderInference catch: (throwable: Throwable) -> Faulty<Error> = ::rethrow,
    @BuilderInference block: RaiseScope<Error>.() -> Unit,
): Faulty<Error> = outcomeOf(catch = catch, block = block)

/**
 * Context runner that encapsulates the result of [block] as a [Faulty].
 *
 * > ***Note:** [catch] will [rethrow] by default.*
 * ```kotlin
 * // Faulty<String>
 * "value".faultyOf { it.uppercase() }
 *
 * // Faulty<Throwable>
 * 3.faultyOf { throw IllegalStateException("error") }
 * ```
 *
 * @receiver Some input type, [In], passed to [block].
 * @param Error The error type of the [Faulty], which must be a subtype of [Any].
 * @param catch Map thrown exceptions to a [Faulty]. (Throws by default).
 * @param block The code to execute.
 * @see faultyOf
 */
public inline fun <In, Error : Any> In.faultyOf(
    @BuilderInference catch: (throwable: Throwable) -> Faulty<Error> = ::rethrow,
    @BuilderInference block: RaiseScope<Error>.(In) -> Unit,
): Faulty<Error> = outcomeOf(catch = catch, block = block)

/**
 * Context runner that uses [RaiseScope] to safely capture raised or thrown errors,
 * and unwraps either the successful [Outcome] or the [fallback], returning [T].
 *
 * > Used in scenarios where you want the advantages of [Outcome], but immediately resolve to its [Ok][Success] type.
 *
 * @param fallback A function that provides a fallback value in case of an raised or thrown exception.
 * @param block The code to execute within the [RaiseScope].
 * @return The result of the [block] if successful, or the result of the [fallback] function if an error occurs.
 */
@OutcomeDsl
public inline fun <T: Any> safely(
    crossinline fallback: (Any) -> T,
    block: RaiseScope<Unit>.() -> T,
) : T = RaiseScope.default {
    fold(block = block, catch = fallback, recover = fallback, transform = ::itself)
}

/**
 * Context runner that uses [RaiseScope] to safely capture raised or thrown errors,
 * and unwraps either the successful [Outcome] or the [fallback], returning [Out].
 *
 * > Used in scenarios where you want the advantages of [Outcome], but immediately resolve to its [Ok][Success] type.
 *
 * @receiver Some input type, [In], passed to [block].
 * @param fallback A function that provides a fallback value in case of an raised or thrown exception.
 * @param block The code to execute within the [RaiseScope].
 * @return The result of the [block] if successful, or the result of the [fallback] function if an error occurs.
 */
@OutcomeDsl
public inline fun <In, Out: Any> In.safely(
    crossinline fallback: (Any) -> Out,
    block: RaiseScope<Unit>.(In) -> Out,
) : Out = RaiseScope.default {
    fold(block = { block(this@safely) }, catch = fallback, recover = fallback, transform = ::itself)
}

/**
 * Context runner that uses [RaiseScope] to safely capture raised
 * or thrown errors, and unwraps either the successful [Outcome] or `null`.
 *
 * > Used in scenarios where you want the advantages of [Outcome], but immediately
 * > resolve to its [Ok][Success] type or `null`.
 *
 * @param block The code to execute within the [RaiseScope].
 * @return The result of the [block] if successful, or `null` if an error occurs.
 */
@OutcomeDsl
public inline fun <T: Any> nullableOf(block: RaiseScope<Unit>.() -> T) : T? = RaiseScope.default {
    fold(block = block, catch = ::nulls, recover = ::nulls, transform = ::itself)
}

/**
 * Context runner that uses [RaiseScope] to safely capture raised
 * or thrown errors, and unwraps either the successful [Outcome] or `null`.
 *
 * > Used in scenarios where you want the advantages of [Outcome], but immediately
 * > resolve to its [Ok][Success] type or `null`.
 *
 * @receiver Some input type, [In], passed to [block].
 * @param block The code to execute within the [RaiseScope].
 * @return The result of the [block] if successful, or `null` if an error occurs.
 */
@OutcomeDsl
public inline fun <In, Out: Any> In.nullableOf(
    block: RaiseScope<Unit>.(In) -> Out,
) : Out? = RaiseScope.default {
    fold(
        block = { block(this@nullableOf) },
        catch = ::nulls,
        recover = ::nulls,
        transform = ::itself,
    )
}
