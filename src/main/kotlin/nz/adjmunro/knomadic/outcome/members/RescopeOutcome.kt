package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.inline.rethrow
import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success
import nz.adjmunro.knomadic.raise.RaiseScope

/**
 * Transforms the encapsulated value if this instance represents [success][Outcome.isSuccess].
 *
 * *If [success] throws an exception, it will be re-encapsulated or re-thrown by [outcomeOf].*
 * *If no exception occurs, or [Outcome] is [failure][Outcome.isFailure], the original [Outcome] is returned unaffected.*
 *
 * ```kotlin
 * outcomeOf { 4 }
 *   .andThen { it * 2 } // Outcome.success(8)
 *   .andThen { check(false) { it } } // Outcome.failure(IllegalStateException("4"))
 *   .andThen { 16 } // Remains Outcome.failure(IllegalStateException("4"))
 * ```
 *
 * ***This is the [outcomeOf] alternative to [Outcome.map].***
 *
 * @see outcomeOf
 * @see Outcome.mapFailure
 * @see Outcome.tryRecover
 */
@OutcomeDsl
public inline fun <In : Any, Out : Any, Error: Any> Outcome<In, Error>.andThen(
    catch: (throwable: Throwable) -> Outcome<Out, Error> = ::rethrow,
    @BuilderInference success: RaiseScope<Error>.(In) -> Out,
): Outcome<Out, Error> = flatMapSuccess { ok: In -> outcomeOf(catch = catch) { success(ok) } }

/**
 * Transforms the encapsulated value if this instance represents [success][Outcome.isSuccess],
 * and the [predicate] returns true. If [predicate] is false, the [Success] is left unchanged.
 *
 * *If [success] throws an exception, it will be re-encapsulated or re-thrown by [outcomeOf].*
 * *If no exception occurs, or [Outcome] is [failure][Outcome.isFailure], the original [Outcome] is returned unaffected.*
 *
 * ```kotlin
 * outcomeOf { 4 }.andIf({ it > 0 }) { it * 2 } // Outcome.success(8)
 * outcomeOf { 4 }.andIf({ it < 0 }) { it * 2 } // Outcome.success(4)
 * ```
 *
 * @see outcomeOf
 * @see Outcome.andThen
 */
public inline fun <Ok : Any, Error : Any> Outcome<Ok, Error>.andIf(
    predicate: (Ok) -> Boolean,
    catch: (throwable: Throwable) -> Outcome<Ok, Error> = ::rethrow,
    @BuilderInference success: RaiseScope<Error>.(Ok) -> Ok,
): Outcome<Ok, Error> = andThen(catch = catch) { ok: Ok -> if (predicate(ok)) success(ok) else ok }


/**
 * Transform [failure][Failure] into [success][Success].
 * - [Success][Outcome.isSuccess] -> `returns` the original caller.
 * - [Failure][Outcome.isFailure] -> [wraps][outcomeOf] & `returns` the result of [onFailure] transformation.
 * - *If [onFailure] throws an exception, it will be caught & wrapped by [outcomeOf].*
 *
 * ***This is the [outcomeOf] alternative to [Outcome.recover].***
 *
 * ```kotlin
 * outcomeOf { 4 } // Outcome.success(4)
 *   .tryRecover { Unit } // No Change - Outcome.success(4)
 *   .andThen { throw FileNotFoundException("test") } // Outcome.failure(FileNotFoundException("test"))
 *   .tryRecover { 7 } // Outcome.success(7)
 * ```
 *
 * @see outcomeOf
 * @see Outcome.andThen
 * @see Outcome.mapFailure
 */
@OutcomeDsl
public inline fun <Ok: Any, ErrorIn: Any, ErrorOut: Any> Outcome<Ok, ErrorIn>.tryRecover(
    catch: (throwable: Throwable) -> Outcome<Ok, ErrorOut> = ::rethrow,
    @BuilderInference failure: RaiseScope<ErrorOut>.(ErrorIn) -> Ok,
): Outcome<Ok, ErrorOut> = flatMapFailure { e: ErrorIn -> outcomeOf(catch = catch) { failure(e) } }
