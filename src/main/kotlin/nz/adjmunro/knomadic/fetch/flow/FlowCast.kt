package nz.adjmunro.knomadic.fetch.flow

import kotlinx.coroutines.flow.map
import nz.adjmunro.knomadic.FetchFlow
import nz.adjmunro.knomadic.fetch.Fetch
import nz.adjmunro.knomadic.fetch.FetchDsl
import nz.adjmunro.knomadic.fetch.members.toFaulty
import nz.adjmunro.knomadic.fetch.members.toMaybe
import nz.adjmunro.knomadic.fetch.members.toOutcome
import nz.adjmunro.knomadic.outcome.FaultyFlow
import nz.adjmunro.knomadic.outcome.MaybeFlow
import nz.adjmunro.knomadic.outcome.OutcomeFlow

/** [Map] a [fetch flow][FetchFlow] to [outcome][Fetch.toOutcome] each emission. */
@FetchDsl
public fun <Ok : Any> FetchFlow<Ok>.mapToOutcome(): OutcomeFlow<Ok, Throwable> {
    return map(transform = Fetch<Ok>::toOutcome)
}

/** [Map] a [fetch flow][FetchFlow] to [maybe][Fetch.toMaybe] each emission. */
@FetchDsl
public fun <Ok : Any> FetchFlow<Ok>.mapToMaybe(): MaybeFlow<Ok> {
    return map(transform = Fetch<Ok>::toMaybe)
}

/** [Map] a [fetch flow][FetchFlow] to [faulty][Fetch.toFaulty] each emission. */
@FetchDsl
public fun FetchFlow<*>.mapToFaulty(): FaultyFlow<Throwable> {
    return map(transform = Fetch<*>::toFaulty)
}
