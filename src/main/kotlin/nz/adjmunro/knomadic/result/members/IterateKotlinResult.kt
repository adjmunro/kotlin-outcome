package nz.adjmunro.knomadic.result.members

import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * - ***If all*** [results][nz.adjmunro.knomadic.result.KotlinResult] in the [Iterable] are [success][Result.isSuccess],
 *   `returns` a new [nz.adjmunro.knomadic.result.KotlinResult] success, with a [List] of each element's encapsulated value.
 * - ***If any*** [results][nz.adjmunro.knomadic.result.KotlinResult] in the [Iterable] are [failure][Result.isFailure],
 *   `returns` a new [nz.adjmunro.knomadic.result.KotlinResult] failure with the result of the [reduce] function.
 */
@KotlinResultDsl
public inline fun <T> Iterable<KotlinResult<T>>.aggregate(
    reduce: (List<Throwable>) -> Throwable = { it.first() },
): KotlinResult<List<T>> {
    contract { callsInPlace(reduce, InvocationKind.AT_MOST_ONCE) }

    val (
        errors: List<KotlinResult<T>>,
        successes: List<KotlinResult<T>>,
    ) = partition(predicate = KotlinResult<T>::isFailure)

    return when {
        errors.isNotEmpty() -> failure(
            exception = reduce(errors.map(transform = KotlinResult<T>::exceptionOrThrow)),
        )

        else -> success(
            value = successes.map(transform = KotlinResult<T>::getOrThrow),
        )
    }
}


/**
 * Serial execution of multiple [resultOf] lambdas.
 * Each lambda is executed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @param blocks The lambdas to execute.
 * @return A list of [KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public fun <T> resultOfEach(
    vararg blocks: () -> T
): List<KotlinResult<T>> = blocks.toList().resultOfEach()

/**
 * Serial execution of multiple [resultOf] lambdas.
 * Each lambda is executed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @receiver An iterable of lambdas to execute.
 * @return A list of [KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public fun <T> Iterable<() -> T>.resultOfEach(): List<KotlinResult<T>> {
    return map { resultOf { it() } }
}

/**
 * Serial execution of multiple [resultOf] lambdas.
 * Each lambda is executed in a separate coroutine, and the results are collected as a sequence of [KotlinResult].
 *
 * @receiver A sequence of lambdas to execute.
 * @return A sequence of [results][KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public fun <T> Sequence<() -> T>.resultOfEach(): Sequence<KotlinResult<T>> {
    return map { resultOf { it() } }
}

/**
 * Serial [fold][KotlinResult.fold] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [List] of [Out].
 *
 * @receiver An [Iterable] of [KotlinResult] to process.
 * @return A list of [KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public inline fun <In, Out> Iterable<KotlinResult<In>>.foldEach(
    success: (In) -> Out,
    failure: (Throwable) -> Out,
): List<Out> {
    return map { it.fold(onSuccess = success, onFailure = failure) }
}

/**
 * Serial [fold][KotlinResult.fold] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [Sequence] of [Out].
 *
 * @receiver A [Sequence] of [KotlinResult] to process.
 * @return A sequence of [results][Out] containing the results of each block.
 */
@KotlinResultDsl
public fun <In, Out> Sequence<KotlinResult<In>>.foldEach(
    success: (In) -> Out,
    failure: (Throwable) -> Out,
): Sequence<Out> {
    return map { it.fold(onSuccess = success, onFailure = failure) }
}

/**
 * Serial [andThen][KotlinResult.andThen] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @receiver An [Iterable] of [KotlinResult] to process.
 * @return A list of [KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public inline fun <In, Out> Iterable<KotlinResult<In>>.andThenEach(
    success: (In) -> Out,
): List<KotlinResult<Out>> {
    return map { it.andThen(onSuccess = success) }
}

/**
 * Serial [andThen][KotlinResult.andThen] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [Sequence] of [KotlinResult].
 *
 * @receiver A [Sequence] of [KotlinResult] to process.
 * @return A sequence of [KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public fun <In, Out> Sequence<KotlinResult<In>>.andThenEach(
    success: (In) -> Out,
): Sequence<KotlinResult<Out>> {
    return map { it.andThen(onSuccess = success) }
}

/**
 * Serial [tryRecover][KotlinResult.tryRecover] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [List] of [KotlinResult].
 *
 * @receiver An [Iterable] of [KotlinResult] to process.
 * @return A list of [KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public inline fun <T> Iterable<KotlinResult<T>>.tryRecoverEach(
    failure: (Throwable) -> T,
): List<KotlinResult<T>> {
    return map { it.tryRecover(onFailure = failure) }
}

/**
 * Serial [tryRecover][KotlinResult.tryRecover] of multiple [KotlinResult]s.
 * Each result is processed in a separate coroutine, and the results are collected as a [Sequence] of [KotlinResult].
 *
 * @receiver A [Sequence] of [KotlinResult] to process.
 * @return A sequence of [KotlinResult] containing the results of each block.
 */
@KotlinResultDsl
public fun <T> Sequence<KotlinResult<T>>.tryRecoverEach(
    failure: (Throwable) -> T,
): Sequence<KotlinResult<T>> {
    return map { it.tryRecover(onFailure = failure) }
}
