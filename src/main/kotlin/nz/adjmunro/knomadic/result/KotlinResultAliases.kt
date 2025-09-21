package nz.adjmunro.knomadic.result

import kotlinx.coroutines.flow.Flow
import nz.adjmunro.knomadic.outcome.Outcome

/**
 * Alias for Kotlin's [Result] type.
 *
 * *Consider using a [Outcome] instead.*
 */
@KotlinResultDsl
public typealias KotlinResult<T> = Result<T>

/**
 * Alias for a [flow][Flow] of a [result][KotlinResult].
 *
 * ```kotlin
 * val result: ResultFlow<String> = flow {
 *     // Emit a Result from inside the Flow
 *     emit(resultOf { "A value." })
 * }
 * ```
 *
 * @see KotlinResult
 */
@KotlinResultDsl
public typealias ResultFlow<Ok> = Flow<KotlinResult<Ok>>
