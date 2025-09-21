package nz.adjmunro.knomadic.outcome.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import nz.adjmunro.knomadic.outcome.OutcomeFlow
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.members.errorOrNull
import nz.adjmunro.knomadic.outcome.members.getOrNull
import nz.adjmunro.knomadic.outcome.members.onFailure
import nz.adjmunro.knomadic.outcome.members.onSuccess

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public fun <Ok : Any, Error : Any> OutcomeFlow<Ok, Error>.filterOnlySuccess(): Flow<Ok> {
    return mapNotNull(transform = Outcome<Ok, Error>::getOrNull)
}

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public fun <Ok : Any, Error : Any> OutcomeFlow<Ok, Error>.filterOnlyFailure(): Flow<Error> {
    return mapNotNull(transform = Outcome<Ok, Error>::errorOrNull)
}

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public inline fun <Ok : Any, Error : Any> OutcomeFlow<Ok, Error>.onEachSuccess(
    crossinline block: suspend (Ok) -> Unit
): OutcomeFlow<Ok, Error> = onEach { it.onSuccess { value: Ok -> block(value) } }

/** TODO: Needs work / doc-comments / testing */
@OutcomeDsl
public inline fun <Ok : Any, Error : Any> OutcomeFlow<Ok, Error>.onEachFailure(
    crossinline block: suspend (Error) -> Unit
): OutcomeFlow<Ok, Error> = onEach { it.onFailure { error: Error -> block(error) } }
