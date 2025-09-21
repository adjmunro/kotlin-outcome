package nz.adjmunro.outcome.fetch.flow

import kotlinx.coroutines.flow.map
import nz.adjmunro.outcome.FetchFlow
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.members.toFaulty
import nz.adjmunro.outcome.fetch.members.toMaybe
import nz.adjmunro.outcome.fetch.members.toOutcome
import nz.adjmunro.outcome.outcome.FaultyFlow
import nz.adjmunro.outcome.outcome.MaybeFlow
import nz.adjmunro.outcome.outcome.OutcomeFlow

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
