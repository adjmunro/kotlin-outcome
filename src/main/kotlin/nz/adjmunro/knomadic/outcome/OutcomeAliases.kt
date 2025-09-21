package nz.adjmunro.knomadic.outcome

import kotlinx.coroutines.flow.Flow
import nz.adjmunro.knomadic.raise.RaiseScope

/**
 * Alias for a [Outcome] used when only the [Error] information is important.
 *
 * *Equivalent to a [`Result<Unit>`][Result], but far more meaningful & useful!*
 */
@OutcomeDsl
public typealias Faulty<Error> = Outcome<Unit, Error>


/**
 * Alias for a [Outcome] used when only the [Ok] information is important.
 *
 * *This is equivalent to a Java `Option` or Kotlin's nullable types, but comes with
 * the facilities provided by [Outcome] (and can be used in all the same places).*
 */
@OutcomeDsl
public typealias Maybe<Ok> = Outcome<Ok, Unit>


/**
 * Alias for a [RaiseScope] context runner used for [Outcome].
 *
 * *If injecting lambdas into [outcomeOf], consider using this as the lambda
 * type to inherit the [RaiseScope] context for your own lambdas.*
 */
@OutcomeDsl
public typealias OutcomeScope<Ok, Error> = RaiseScope<Error>.() -> Ok

/**
 * Alias for a [RaiseScope] context runner used for [Maybe].
 *
 * *If injecting lambdas into [maybeOf], consider using this as the lambda
 * type to inherit the [RaiseScope] context for your own lambdas.*
 */
@OutcomeDsl
public typealias MaybeScope<Ok> = RaiseScope<Any>.() -> Ok

/**
 * Alias for a [RaiseScope] context runner used for [Faulty].
 *
 * *If injecting lambdas into [faultyOf], consider using this as the lambda
 * type to inherit the [RaiseScope] context for your own lambdas.*
 */
@OutcomeDsl
public typealias FaultyScope<Error> = RaiseScope<Error>.() -> Unit


/**
 * Alias for a [flow][Flow] of a [faulty][Faulty] result.
 *
 * ```kotlin
 * val faulty: FaultyFlow<String> = flow {
 *     // Emit a Faulty from inside the Flow
 *     emit(faultyOf { "An error occurred." })
 * }
 * ```
 *
 * @see Faulty
 */
@OutcomeDsl
public typealias FaultyFlow<Error> = Flow<Faulty<Error>>

/**
 * Alias for a [flow][Flow] of a [maybe][Maybe] result.
 *
 * ```kotlin
 * val maybe: MaybeFlow<String> = flow {
 *     // Emit a Maybe from inside the Flow
 *     emit(maybeOf { "A value." })
 * }
 * ```
 *
 * @see Maybe
 */
@OutcomeDsl
public typealias MaybeFlow<Ok> = Flow<Maybe<Ok>>

/**
 * Alias for a [flow][Flow] of an [outcome][Outcome] result.
 *
 * ```kotlin
 * val outcome: OutcomeFlow<String, Int> = flow {
 *     // Emit an Outcome from inside the Flow
 *     emit(outcomeOf { 42 })
 * }
 * ```
 *
 * @see Outcome
 */
@OutcomeDsl
public typealias OutcomeFlow<Ok, Error> = Flow<Outcome<Ok, Error>>
