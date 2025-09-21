package nz.adjmunro.outcome.outcome.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.adjmunro.outcome.outcome.OutcomeFlow
import nz.adjmunro.outcome.outcome.Outcome
import nz.adjmunro.outcome.outcome.OutcomeDsl
import nz.adjmunro.outcome.outcome.members.collapse
import nz.adjmunro.outcome.outcome.members.fold

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
