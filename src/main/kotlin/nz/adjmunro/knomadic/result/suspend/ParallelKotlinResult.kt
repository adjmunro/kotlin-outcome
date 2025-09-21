package nz.adjmunro.knomadic.result.suspend

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import nz.adjmunro.inline.parallelMap
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import nz.adjmunro.knomadic.result.members.andThen
import nz.adjmunro.knomadic.result.members.resultOf
import nz.adjmunro.knomadic.result.members.tryRecover

/**
 * Parallel execution of multiple [resultOf] lambdas.
 * Each lambda is executed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @param blocks The lambdas to execute in parallel.
 * @return A list of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <T> parallelResultOf(
    vararg blocks: suspend CoroutineScope.() -> T
): List<KotlinResult<T>> = blocks.toList().parallelResult()

/**
 * Parallel execution of multiple [resultOf] lambdas.
 * Each lambda is executed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @receiver An iterable of lambdas to execute in parallel.
 * @return A list of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <T> Iterable<suspend CoroutineScope.() -> T>.parallelResult(): List<KotlinResult<T>> {
    return parallelMap { resultOf { it() } }
}

/**
 * Parallel execution of multiple [resultOf] lambdas.
 * Each lambda is executed in a separate coroutine, and the results are collected as a sequence of [Deferred][Deferred] [Results][KotlinResult].
 *
 * @receiver A sequence of lambdas to execute in parallel.
 * @return A [List] of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <T> Sequence<suspend CoroutineScope.() -> T>.parallelResult(): List<KotlinResult<T>> {
    return parallelMap { resultOf { it() } }
}

/**
 * Parallel [fold][KotlinResult.fold] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [List] of [Out].
 *
 * @receiver An [Iterable] of [KotlinResult] to process in parallel.
 * @return A list of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <In, Out> Iterable<KotlinResult<In>>.parallelFold(
    success: suspend CoroutineScope.(In) -> Out,
    failure: suspend CoroutineScope.(Throwable) -> Out,
): List<Out> {
    return parallelMap {
        it.fold(
            onSuccess = { value: In -> success(value) },
            onFailure = { error: Throwable -> failure(error) },
        )
    }
}

/**
 * Parallel [fold][KotlinResult.fold] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [Sequence] of [Deferred][Deferred] [Out][Out].
 *
 * @receiver A [Sequence] of [KotlinResult] to process in parallel.
 * @return A [List] of [Out] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <In, Out> Sequence<KotlinResult<In>>.parallelFold(
    success: suspend CoroutineScope.(In) -> Out,
    failure: suspend CoroutineScope.(Throwable) -> Out,
): List<Out> {
    return parallelMap {
        it.fold(
            onSuccess = { value: In -> success(value) },
            onFailure = { error: Throwable -> failure(error) },
        )
    }
}

/**
 * Parallel [andThen][KotlinResult.andThen] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @receiver An [Iterable] of [KotlinResult] to process in parallel.
 * @return A list of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <In, Out> Iterable<KotlinResult<In>>.parallelAndThen(
    success: suspend CoroutineScope.(In) -> Out,
): List<KotlinResult<Out>> {
    return parallelMap { it.andThen { value: In -> success(value) } }
}

/**
 * Parallel [andThen][KotlinResult.andThen] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [Sequence] of [Deferred][Deferred] [KotlinResult].
 *
 * @receiver A [Sequence] of [KotlinResult] to process in parallel.
 * @return A [List] of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <In, Out> Sequence<KotlinResult<In>>.parallelAndThen(
    success: suspend CoroutineScope.(In) -> Out,
): List<KotlinResult<Out>> {
    return parallelMap { it.andThen { value: In -> success(value) } }
}

/**
 * Parallel [tryRecover][KotlinResult.tryRecover] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @receiver An [Iterable] of [KotlinResult] to process in parallel.
 * @return A list of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <T> Iterable<KotlinResult<T>>.parallelTryRecover(
    failure: suspend CoroutineScope.(Throwable) -> T,
): List<KotlinResult<T>> {
    return parallelMap { it.tryRecover { error: Throwable -> failure(error) } }
}

/**
 * Parallel [tryRecover][KotlinResult.tryRecover] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [Sequence] of [Deferred][Deferred] [KotlinResult].
 *
 * @receiver A [Sequence] of [KotlinResult] to process in parallel.
 * @return A [List] of [KotlinResult] containing the results of each block.
 * @see parallelMap
 */
@KotlinResultDsl
public suspend fun <T> Sequence<KotlinResult<T>>.parallelTryRecover(
    failure: suspend CoroutineScope.(Throwable) -> T,
): List<KotlinResult<T>> {
    return parallelMap { it.tryRecover { error: Throwable -> failure(error) } }
}
