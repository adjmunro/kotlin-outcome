package nz.adjmunro.knomadic.outcome.flow

import kotlinx.coroutines.flow.map
import nz.adjmunro.knomadic.outcome.OutcomeFlow
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.members.mapFailure
import nz.adjmunro.knomadic.outcome.members.mapSuccess

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public inline fun <In: Any, Out: Any, Error: Any> OutcomeFlow<In, Error>.mapSuccess(
    crossinline transform: suspend (In) -> Out
): OutcomeFlow<Out, Error> = map { it.mapSuccess { transform(it) } }

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public inline fun <Ok: Any, ErrorIn: Any, ErrorOut: Any> OutcomeFlow<Ok, ErrorIn>.mapFailure(
    crossinline transform: suspend (ErrorIn) -> ErrorOut
): OutcomeFlow<Ok, ErrorOut> = map { it.mapFailure { transform(it) } }
