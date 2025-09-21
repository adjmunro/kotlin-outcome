package nz.adjmunro.knomadic.result.suspend

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import nz.adjmunro.knomadic.result.ResultFlow
import nz.adjmunro.knomadic.result.members.andIf
import nz.adjmunro.knomadic.result.members.andThen
import nz.adjmunro.knomadic.result.members.mapFailure
import nz.adjmunro.knomadic.result.members.tryRecover

/**
 * Returns a flow containing the [output][Out] of applying [KotlinResult.map] to each value of the original flow.
 *
 * @see Flow.map
 */
@KotlinResultDsl
public inline fun <In, Out> ResultFlow<In>.mapSuccess(
    crossinline success: suspend (In) -> Out,
): Flow<Result<Out>> {
    return map { it.map { value: In -> success(value) } }
}

/**
 * Returns a flow containing the [output][Out] of applying [KotlinResult.andThen] to each value of the original flow.
 *
 * @see Flow.map
 */
@KotlinResultDsl
public inline fun <In, Out> ResultFlow<In>.andThen(
    crossinline success: suspend (In) -> Out,
): Flow<Result<Out>> {
    return map { it.andThen { value: In -> success(value) } }
}

/**
 * Returns a flow containing the [output][Out] of applying [KotlinResult.andIf] to each value of the original flow.
 *
 * @see Flow.map
 */
@KotlinResultDsl
public inline fun <T> ResultFlow<T>.andThen(
    crossinline predicate: (T) -> Boolean,
    crossinline success: suspend (T) -> T,
): Flow<Result<T>> {
    return map { it.andIf(predicate) { value: T -> success(value) } }
}

/**
 * Returns a flow containing the output of applying [KotlinResult.mapFailure] to each value of the original flow.
 *
 * @see Flow.map
 */
@KotlinResultDsl
public inline fun <T> ResultFlow<T>.mapFailure(
    crossinline failure: suspend (Throwable) -> Throwable,
): Flow<Result<T>> {
    return map { it.mapFailure { error: Throwable -> failure(error) } }
}

/**
 * Returns a flow containing the [output][T] of applying [KotlinResult.tryRecover] to each value of the original flow.
 *
 * @see Flow.map
 */
@KotlinResultDsl
public inline fun <T> ResultFlow<T>.tryRecover(
    crossinline failure: suspend (Throwable) -> T,
): Flow<Result<T>> {
    return map { it.tryRecover { error: Throwable -> failure(error) } }
}
