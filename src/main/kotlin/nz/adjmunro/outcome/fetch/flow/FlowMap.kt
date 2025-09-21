package nz.adjmunro.outcome.fetch.flow

import kotlinx.coroutines.flow.map
import nz.adjmunro.outcome.fetch.FetchFlow
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished
import nz.adjmunro.outcome.fetch.members.mapFetching
import nz.adjmunro.outcome.fetch.members.mapFinished
import nz.adjmunro.outcome.fetch.members.mapToCache

/** [Map] a [fetch flow][FetchFlow] to [mapFetching][Fetch.mapFetching] each emission. */
@FetchDsl
public inline fun <In : Out, Out : Any> FetchFlow<In>.mapFetching(
    crossinline transform: suspend Fetching<In>.() -> Out?,
): FetchFlow<Out> = map { it.mapFetching { transform() } }

/** [Map] a [fetch flow][FetchFlow] to [mapFinished][Fetch.mapFinished] each emission. */
@FetchDsl
public inline fun <In : Out, Out : Any> FetchFlow<In>.mapFinished(
    crossinline transform: suspend Finished<In>.() -> Out,
): FetchFlow<Out> = map { it.mapFinished { transform() } }

/** [Map] a [fetch flow][FetchFlow] to [mapToCache][Fetch.mapToCache] each emission. */
@FetchDsl
public inline fun <In : Out, Out : Any> FetchFlow<In>.mapToCache(
    crossinline transform: suspend Finished<In>.() -> Out = { result },
): FetchFlow<Out> = map { it.mapToCache { transform() } }
