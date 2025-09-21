package nz.adjmunro.outcome.fetch.flow

import kotlinx.coroutines.flow.map
import nz.adjmunro.outcome.FetchFlow
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.members.flatten

/** [Map] a [fetch flow][FetchFlow] to [flatten][Fetch.flatten] each emission. */
@FetchDsl
public fun <T : Any> FetchFlow<Fetch<T>>.flatten(): FetchFlow<T> = map { it.flatten }
