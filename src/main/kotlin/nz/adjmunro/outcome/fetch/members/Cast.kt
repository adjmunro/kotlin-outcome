package nz.adjmunro.outcome.fetch.members

import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.outcome.Failure
import nz.adjmunro.outcome.outcome.Faulty
import nz.adjmunro.outcome.outcome.Maybe
import nz.adjmunro.outcome.outcome.Outcome
import nz.adjmunro.outcome.outcome.members.catch
import nz.adjmunro.outcome.outcome.members.faultyOf
import nz.adjmunro.outcome.outcome.members.maybeOf
import nz.adjmunro.outcome.result.KotlinResult
import nz.adjmunro.outcome.result.members.resultOf

/**
 * Convert a [Fetch] to an [Outcome] by [catching][catch] any exceptions thrown by [getOrThrow].
 *
 * @return An [Outcome] containing the [result][nz.adjmunro.outcome.fetch.Finished.result]
 * or [cache][nz.adjmunro.outcome.fetch.Fetching.cache] of the fetch or a [Throwable] if an error occurred.
 */
@FetchDsl
public fun <Ok : Any> Fetch<Ok>.toOutcome(): Outcome<Ok, Throwable> = catch { getOrThrow() }


/**
 * Convert a [Fetch] to a [Maybe] by [catching and suppressing][maybeOf] any exceptions thrown by [getOrThrow].
 *
 * @return A [Maybe] containing the [result][nz.adjmunro.outcome.fetch.Finished.result]
 * or [cache][nz.adjmunro.outcome.fetch.Fetching.cache] of the fetch or a [Unit] if an error occurred.
 */
@FetchDsl
public fun <Ok : Any> Fetch<Ok>.toMaybe(): Maybe<Ok> = maybeOf { getOrThrow() }

/**
 * Convert a [Fetch] to a [Faulty] by [catching and wrapping][faultyOf] any exceptions thrown by [getOrThrow].
 *
 * @return A [Faulty] containing the [Unit] or a [Throwable] if an error occurred.
 */
@FetchDsl
public fun Fetch<*>.toFaulty(): Faulty<Throwable> = faultyOf(catch = ::Failure) { getOrThrow() }

/**
 * Convert a [Fetch] to a [KotlinResult] by catching the [resultOf] any exceptions thrown by [getOrThrow].
 *
 * @return A [KotlinResult] containing the [result][nz.adjmunro.outcome.fetch.Finished.result]
 * or [cache][nz.adjmunro.outcome.fetch.Fetching.cache] of the fetch or a [Throwable] if an error occurred.
 */
@FetchDsl
public fun <T: Any> Fetch<T>.toKotlinResult(): KotlinResult<T> = resultOf { getOrThrow() }
