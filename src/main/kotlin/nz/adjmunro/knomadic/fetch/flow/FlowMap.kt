package nz.adjmunro.knomadic.fetch.flow

import kotlinx.coroutines.flow.map
import nz.adjmunro.knomadic.FetchFlow
import nz.adjmunro.knomadic.fetch.Fetch
import nz.adjmunro.knomadic.fetch.FetchDsl
import nz.adjmunro.knomadic.fetch.Fetching
import nz.adjmunro.knomadic.fetch.Finished
import nz.adjmunro.knomadic.fetch.members.mapFetching
import nz.adjmunro.knomadic.fetch.members.mapFinished
import nz.adjmunro.knomadic.fetch.members.mapToCache

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
