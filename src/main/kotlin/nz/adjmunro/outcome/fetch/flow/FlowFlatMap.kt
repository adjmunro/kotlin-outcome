package nz.adjmunro.outcome.fetch.flow

import kotlinx.coroutines.flow.map
import nz.adjmunro.outcome.fetch.FetchFlow
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished
import nz.adjmunro.outcome.fetch.Prefetch
import nz.adjmunro.outcome.fetch.members.flatMapFetching
import nz.adjmunro.outcome.fetch.members.flatMapFinished
import nz.adjmunro.outcome.fetch.members.flatMapPrefetch

/** [Map] a [fetch flow][FetchFlow] to [flatMapPrefetch][Fetch.flatMapPrefetch] each emission. */
@FetchDsl
public inline fun <In : Out, Out : Any> FetchFlow<In>.flatMapPrefetch(
    crossinline transform: suspend Prefetch.() -> Fetch<Out>,
): FetchFlow<Out> = map { it.flatMapPrefetch { transform() } }

/** [Map] a [fetch flow][FetchFlow] to [flatMapFetching][Fetch.flatMapFetching] each emission. */
@FetchDsl
public inline fun <In : Out, Out : Any> FetchFlow<In>.flatMapFetching(
    crossinline transform: suspend Fetching<In>.() -> Fetch<Out>,
): FetchFlow<Out> = map { it.flatMapFetching { transform() } }

/** [Map] a [fetch flow][FetchFlow] to [flatMapFinished][Fetch.flatMapFinished] each emission. */
@FetchDsl
public inline fun <In : Out, Out : Any> FetchFlow<In>.flatMapFinished(
    crossinline transform: suspend Finished<In>.() -> Fetch<Out>,
): FetchFlow<Out> = map { it.flatMapFinished { transform() } }
