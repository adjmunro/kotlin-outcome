package nz.adjmunro.outcome.members

import kotlinx.coroutines.flow.Flow
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.raise.RaiseScope

/**
 * Alias for [Outcome][nz.adjmunro.outcome.Outcome] used when only the error is useful — the success is irrelevant.
 *
 * Equivalent to `Result<Unit>`, but retains the typed [Error] and the full
 * [Outcome][nz.adjmunro.outcome.Outcome] API.
 */
public typealias Fault<Error> = Outcome<Unit, Error>

/**
 * Alias for [Outcome][nz.adjmunro.outcome.Outcome] used when only the success value is useful — the error is irrelevant.
 *
 * Similar to a Java `Optional` or Kotlin's nullable types, but backed by the full
 * [Outcome][nz.adjmunro.outcome.Outcome] API.
 */
public typealias Maybe<Ok> = Outcome<Ok, Unit>

/**
 * Alias for a [Flow] of [Fault][nz.adjmunro.outcome.members.Fault] values.
 *
 * ```kotlin
 * val faultFlow: FaultFlow<String> = flow {
 *     emit(faultOf { raise { "An error occurred." } })
 * }
 * ```
 *
 * @see nz.adjmunro.outcome.members.Fault
 */
public typealias FaultFlow<Error> = Flow<Fault<Error>>

/**
 * Alias for a [Flow] of [Maybe][nz.adjmunro.outcome.members.Maybe] values.
 *
 * ```kotlin
 * val maybeFlow: MaybeFlow<String> = flow {
 *     emit(maybeOf { "A value." })
 * }
 * ```
 *
 * @see nz.adjmunro.outcome.members.Maybe
 */
public typealias MaybeFlow<Ok> = Flow<Maybe<Ok>>

/**
 * Alias for a [Flow] of [Outcome][nz.adjmunro.outcome.Outcome] values.
 *
 * ```kotlin
 * val outcomeFlow: OutcomeFlow<Int, String> = flow {
 *     emit(outcomeOf { 42 })
 * }
 * ```
 *
 * @see nz.adjmunro.outcome.Outcome
 */
public typealias OutcomeFlow<Ok, Error> = Flow<Outcome<Ok, Error>>
