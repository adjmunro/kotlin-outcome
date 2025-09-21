package nz.adjmunro.knomadic.result.suspend

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import nz.adjmunro.knomadic.result.ResultFlow
import nz.adjmunro.knomadic.result.members.exceptionOrThrow

/**
 * Returns a flow containing only [successful][Result.isSuccess] values of the original flow.
 *
 * @see Flow.filter
 */
@KotlinResultDsl
public fun <T> ResultFlow<T>.filterSuccess(): Flow<T> {
    return filter { it.isSuccess }.map { it.getOrThrow() }
}

/**
 * Returns a flow containing only [failed][Result.isFailure] values of the original flow.
 *
 * @see Flow.filter
 */
@KotlinResultDsl
public fun <T> ResultFlow<T>.filterFailure(): Flow<Throwable> {
    return filter { it.isFailure }.map { it.exceptionOrThrow() }
}

/**
 * Returns a flow that invokes the given action if it is a [success][Result.isSuccess]
 * **before** each value of the upstream flow is emitted downstream.
 *
 * @see Flow.onEach
 */
@KotlinResultDsl
public inline fun <T> ResultFlow<T>.onEachSuccess(
    crossinline action: suspend (T) -> Unit,
): ResultFlow<T> {
    return onEach { if(it.isSuccess) action(it.getOrThrow()) }
}

/**
 * Returns a flow that invokes the given action if it is a [failure][Result.isFailure]
 * **before** each value of the upstream flow is emitted downstream.
 *
 * @see Flow.onEach
 */
@KotlinResultDsl
public inline fun <T> ResultFlow<T>.onEachFailure(
    crossinline action: suspend (Throwable) -> Unit,
): ResultFlow<T> {
    return onEach { if(it.isFailure) action(it.exceptionOrThrow()) }
}

/**
 * Returns a flow that invokes the given actions for each [success][Result.isSuccess] and
 * [failure][Result.isFailure] **before** each value of the upstream flow is emitted downstream.
 *
 * @see Flow.onEach
 */
@KotlinResultDsl
public inline fun <T> ResultFlow<T>.onEachResult(
    crossinline success: suspend (T) -> Unit,
    crossinline failure: suspend (Throwable) -> Unit,
): ResultFlow<T> {
    return onEach {
        it.fold(
            onSuccess = { value: T -> success(value) },
            onFailure = { error: Throwable -> failure(error) },
        )
    }
}
