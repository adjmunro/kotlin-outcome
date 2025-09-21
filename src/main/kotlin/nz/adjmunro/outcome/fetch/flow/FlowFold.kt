package nz.adjmunro.outcome.fetch.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.adjmunro.outcome.FetchFlow
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished
import nz.adjmunro.outcome.fetch.Prefetch
import nz.adjmunro.outcome.fetch.members.fold

/**
 * [Fold][Fetch.fold] a [FetchFlow] into a [Flow] of [Output].
 *
 * *Fold can be used to `map` or `flatMap` all [Fetch] variants,
 * but it also can output any type.*
 *
 * @receiver The [FetchFlow] to fold.
 * @param Data The type of the [Fetch] value.
 * @param Output The type of the folded value.
 * @param prefetch The lambda to transform the [Prefetch].
 * @param fetching The lambda to transform the [Fetching].
 * @param finished The lambda to transform the [Finished].
 * @return A [Flow] of the folded value, as type [Output].
 */
@FetchDsl
public inline fun <Data : Any, Output> FetchFlow<Data>.fold(
    crossinline prefetch: suspend Prefetch.() -> Output,
    crossinline fetching: suspend Fetching<Data>.() -> Output,
    crossinline finished: suspend Finished<Data>.() -> Output,
): Flow<Output> = map { fetch: Fetch<Data> ->
    fetch.fold(prefetch = { prefetch() }, fetching = { fetching() }, finished = { finished() })
}
