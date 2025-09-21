package nz.adjmunro.knomadic

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import nz.adjmunro.knomadic.fetch.Fetch
import nz.adjmunro.knomadic.fetch.fetch
import nz.adjmunro.knomadic.outcome.Faulty
import nz.adjmunro.knomadic.outcome.Maybe
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.result.KotlinResult


/**
 * Alias for a [Flow] of [Fetch] statuses.
 * @see fetch
 */
@KnomadicDsl
public typealias FetchFlow<T> = Flow<Fetch<T>>

/**
 * Alias for a [FlowCollector] of [Fetch] statuses.
 *
 * *For internal-use only!*
 *
 * @see fetch
 */
@KnomadicDsl
internal typealias FetchCollector<T> = FlowCollector<Fetch<T>>

/**
 * Alias for a [flow][Flow] that [fetches][Fetch] a [faulty][Faulty] result.
 *
 * ```kotlin
 * val faulty: FaultyFetch<String> = fetch {
 *    // Return a Faulty from inside the Fetch runner
 *    faultyOf { "An error occurred." }
 * }
 * ```
 *
 * @see fetch
 * @see Faulty
 */
@KnomadicDsl
public typealias FaultyFetch<Error> = Flow<Fetch<Faulty<Error>>>

/**
 * Alias for a [flow][Flow] that [fetches][Fetch] a [maybe][Maybe] result.
 *
 * ```kotlin
 * val maybe: MaybeFetch<String> = fetch {
 *   // Return a Maybe from inside the Fetch runner
 *   maybeOf { "A value." }
 * }
 * ```
 *
 * @see fetch
 * @see Maybe
 */
@KnomadicDsl
public typealias MaybeFetch<Ok> = Flow<Fetch<Maybe<Ok>>>

/**
 * Alias for a [flow][Flow] that [fetches][Fetch] an [outcome][Outcome] result.
 *
 * ```kotlin
 * val outcome: OutcomeFetch<String, Int> = fetch {
 *   // Return an Outcome from inside the Fetch runner
 *   outcomeOf { 42 }
 * }
 * ```
 *
 * @see fetch
 * @see Outcome
 */
@KnomadicDsl
public typealias OutcomeFetch<Ok, Error> = Flow<Fetch<Outcome<Ok, Error>>>

/**
 * Alias for a [flow][Flow] that [fetches][Fetch] a [result][KotlinResult].
 *
 * ```kotlin
 * val result: ResultFetch<String> = fetch {
 *   // Return a Result from inside the Fetch runner
 *   resultOf { "A value." }
 * }
 * ```
 *
 * @see fetch
 * @see KotlinResult
 */
@KnomadicDsl
public typealias ResultFetch<Ok> = Flow<Fetch<KotlinResult<Ok>>>
