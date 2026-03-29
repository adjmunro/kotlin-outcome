package nz.adjmunro.outcome.members

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.transform
import nz.adjmunro.outcome.Outcome

/**
 * Returns a [Flow][kotlinx.coroutines.flow.Flow] containing only the
 * [value][nz.adjmunro.outcome.Success.value] of each [Success][nz.adjmunro.outcome.Success] outcome.
 * [Failure][nz.adjmunro.outcome.Failure] outcomes are filtered out.
 */
public fun <Ok, Error> OutcomeFlow<Ok, Error>.filterOnlySuccess(): Flow<Ok> = transform {
    if (it.isSuccess()) emit(value = it.value)
}

/**
 * Returns a [Flow][kotlinx.coroutines.flow.Flow] containing only the
 * [error][nz.adjmunro.outcome.Failure.error] of each [Failure][nz.adjmunro.outcome.Failure] outcome.
 * [Success][nz.adjmunro.outcome.Success] outcomes are filtered out.
 */
public fun <Ok, Error> OutcomeFlow<Ok, Error>.filterOnlyFailure(): Flow<Error> = transform {
    if (it.isFailure()) emit(value = it.error)
}

/**
 * Returns an [OutcomeFlow][nz.adjmunro.outcome.members.OutcomeFlow] that invokes [block] for each
 * [Success][nz.adjmunro.outcome.Success] outcome **before** emitting it downstream.
 * [Failure][nz.adjmunro.outcome.Failure] outcomes pass through unaffected.
 */
public inline fun <Ok, Error> OutcomeFlow<Ok, Error>.onEachSuccess(
    crossinline block: suspend (Ok) -> Unit
): OutcomeFlow<Ok, Error> = transform {
    if (it.isSuccess()) {
        block(it.value)
    }

    emit(value = it)
}

/**
 * Returns an [OutcomeFlow][nz.adjmunro.outcome.members.OutcomeFlow] that invokes [block] for each
 * [Failure][nz.adjmunro.outcome.Failure] outcome **before** emitting it downstream.
 * [Success][nz.adjmunro.outcome.Success] outcomes pass through unaffected.
 */
public inline fun <Ok, Error> OutcomeFlow<Ok, Error>.onEachFailure(
    crossinline block: suspend (Error) -> Unit
): OutcomeFlow<Ok, Error> = transform {
    if (it.isFailure()) {
        block(it.error)
    }

    emit(value = it)
}

/**
 * Returns a new [OutcomeFlow][nz.adjmunro.outcome.members.OutcomeFlow] with each
 * [Success][nz.adjmunro.outcome.Success] value transformed by [transform].
 * [Failure][nz.adjmunro.outcome.Failure] outcomes pass through unaffected.
 *
 * @see mapSuccess
 */
public inline fun <In, Out, Error> OutcomeFlow<In, Error>.mapSuccess(
    crossinline transform: suspend (In) -> Out
): OutcomeFlow<Out, Error> = map { outcome: Outcome<In, Error> ->
    outcome.mapSuccess { ok: In -> transform(ok) }
}

/**
 * Returns a new [OutcomeFlow][nz.adjmunro.outcome.members.OutcomeFlow] with each
 * [Failure][nz.adjmunro.outcome.Failure] error transformed by [transform].
 * [Success][nz.adjmunro.outcome.Success] outcomes pass through unaffected.
 *
 * @see mapFailure
 */
public inline fun <Ok, ErrorIn, ErrorOut> OutcomeFlow<Ok, ErrorIn>.mapFailure(
    crossinline transform: suspend (ErrorIn) -> ErrorOut
): OutcomeFlow<Ok, ErrorOut> = map { outcome: Outcome<Ok, ErrorIn> ->
    outcome.mapFailure { error: ErrorIn -> transform(error) }
}

/**
 * Returns a [Flow][kotlinx.coroutines.flow.Flow] of [Output] by applying [success] or [failure]
 * to each [Outcome][nz.adjmunro.outcome.Outcome] in the upstream flow.
 *
 * @see fold
 */
public inline fun <Ok, Error, Output> OutcomeFlow<Ok, Error>.foldOutcome(
    crossinline success: suspend (Ok) -> Output,
    crossinline failure: suspend (Error) -> Output,
): Flow<Output> = map { it.fold(success = { success(value) }, failure = { failure(error) }) }

/**
 * Returns a [Flow][kotlinx.coroutines.flow.Flow] that collapses each
 * [Outcome][nz.adjmunro.outcome.Outcome] to its nearest common [Ancestor] type.
 *
 * @see collapse
 */
public fun <Ancestor, Ok: Ancestor, Error: Ancestor> OutcomeFlow<Ok, Error>.collapseOutcome(): Flow<Ancestor> =
    map(transform = Outcome<Ok, Error>::collapse)
