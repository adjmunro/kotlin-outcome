package nz.adjmunro.outcome.outcome.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import nz.adjmunro.outcome.outcome.OutcomeFlow
import nz.adjmunro.outcome.outcome.Outcome
import nz.adjmunro.outcome.outcome.OutcomeDsl
import nz.adjmunro.outcome.outcome.members.errorOrNull
import nz.adjmunro.outcome.outcome.members.getOrNull
import nz.adjmunro.outcome.outcome.members.onFailure
import nz.adjmunro.outcome.outcome.members.onSuccess

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
