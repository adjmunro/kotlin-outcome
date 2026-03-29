package nz.adjmunro.outcome.result

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach

/**
 * Returns a [Flow][kotlinx.coroutines.flow.Flow] of unwrapped success values, dropping all
 * [failures][kotlin.Result.isFailure] from the original flow.
 */
public fun <T> KotlinResultFlow<T>.filterSuccess(): Flow<T> {
    return filter { it.isSuccess }.map { it.getOrThrow() }
}

/**
 * Returns a [Flow][kotlinx.coroutines.flow.Flow] of unwrapped [Throwable] values, dropping all
 * [successes][kotlin.Result.isSuccess] from the original flow.
 */
public fun <T> KotlinResultFlow<T>.filterFailure(): Flow<Throwable> {
    return filter { it.isFailure }.map { it.exceptionOrThrow() }
}

/**
 * Invokes [action] with the unwrapped success value **before** each [successful][kotlin.Result.isSuccess]
 * element is emitted downstream. Failures pass through unaffected.
 */
public inline fun <T> KotlinResultFlow<T>.onEachSuccess(
    crossinline action: suspend (T) -> Unit,
): KotlinResultFlow<T> {
    return onEach { if(it.isSuccess) action(it.getOrThrow()) }
}

/**
 * Invokes [action] with the unwrapped [Throwable] **before** each [failed][kotlin.Result.isFailure]
 * element is emitted downstream. Successes pass through unaffected.
 */
public inline fun <T> KotlinResultFlow<T>.onEachFailure(
    crossinline action: suspend (Throwable) -> Unit,
): KotlinResultFlow<T> {
    return onEach { if(it.isFailure) action(it.exceptionOrThrow()) }
}

/**
 * Invokes [success] or [failure] **before** each element is emitted downstream, depending on whether the
 * element is a [success][kotlin.Result.isSuccess] or [failure][kotlin.Result.isFailure].
 */
public inline fun <T> KotlinResultFlow<T>.onEachResult(
    crossinline success: suspend (T) -> Unit,
    crossinline failure: suspend (Throwable) -> Unit,
): KotlinResultFlow<T> {
    return onEach {
        it.fold(
            onSuccess = { value: T -> success(value) },
            onFailure = { error: Throwable -> failure(error) },
        )
    }
}


/**
 * Maps each element of the flow to [Out] by folding over its [success][kotlin.Result.isSuccess] or
 * [failure][kotlin.Result.isFailure] state. Equivalent to calling [Result.fold][kotlin.Result.fold] on each element.
 *
 * @param In The success value type of each result in the source flow.
 * @param Out The output type produced by [success] and [failure].
 */
public inline fun <In, Out> KotlinResultFlow<In>.foldResult(
    crossinline success: suspend (In) -> Out,
    crossinline failure: suspend (Throwable) -> Out,
): Flow<Out> {
    return map { it: KotlinResult<In> ->
        it.fold(
            onSuccess = { value: In -> success(value) },
            onFailure = { error: Throwable -> failure(error) },
        )
    }
}

/**
 * Flattens a flow of nested [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] values by applying
 * [flatten][nz.adjmunro.outcome.result.flatten] to each element.
 *
 * @see nz.adjmunro.outcome.result.flatten
 */
public fun <T> KotlinResultFlow<KotlinResult<T>>.flattenResult(): KotlinResultFlow<T> {
    return map { it.flatten() }
}
