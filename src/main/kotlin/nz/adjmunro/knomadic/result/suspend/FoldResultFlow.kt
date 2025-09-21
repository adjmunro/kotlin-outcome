package nz.adjmunro.knomadic.result.suspend

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import nz.adjmunro.knomadic.result.ResultFlow
import nz.adjmunro.knomadic.result.members.flatten

/**
 * Returns a flow containing the [output][Out] of applying [KotlinResult.fold] to each value of the original flow.
 *
 * @see Flow.map
 */
@KotlinResultDsl
public inline fun <In, Out> ResultFlow<In>.foldResult(
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
 * Returns a flow that [flattens][KotlinResult.flatten] a nested [KotlinResult] for each value of the original flow.
 *
 * @see Flow.map
 */
@KotlinResultDsl
public fun <T> ResultFlow<Result<T>>.flatten(): ResultFlow<T> {
    return map { it.flatten() }
}
