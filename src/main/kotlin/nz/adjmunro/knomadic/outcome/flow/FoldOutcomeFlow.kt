package nz.adjmunro.knomadic.outcome.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.adjmunro.knomadic.outcome.OutcomeFlow
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.members.collapse
import nz.adjmunro.knomadic.outcome.members.fold

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public fun <Ancestor : Any, Ok: Ancestor, Error: Ancestor> OutcomeFlow<Ok, Error>.collapse(): Flow<Ancestor> =
    map(transform = Outcome<Ok, Error>::collapse)

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public inline fun <Ok: Any, Error: Any, Output> OutcomeFlow<Ok, Error>.foldOutcome(
    crossinline success: suspend (Ok) -> Output,
    crossinline failure: suspend (Error) -> Output,
): Flow<Output> = map { it.fold(success = { success(value) }, failure = { failure(error) }) }
