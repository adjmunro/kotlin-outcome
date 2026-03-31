@file:Suppress("unused", "NOTHING_TO_INLINE")

package nz.adjmunro.outcome.result

import nz.adjmunro.outcome.inline.itself
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Transforms the encapsulated [Throwable] if this instance represents [failure][kotlin.Result.isFailure].
 *
 * Successes pass through unchanged. Any exception thrown by [transform] propagates directly — it is not caught.
 *
 * @see nz.adjmunro.outcome.result.andThen
 */
public inline fun <T> KotlinResult<T>.mapFailure(transform: (Throwable) -> Throwable): KotlinResult<T> {
    contract { callsInPlace(lambda = transform, kind = InvocationKind.AT_MOST_ONCE) }
    return fold(onSuccess = ::success, onFailure = { kotlinFailure(throwable = transform(it)) })
}

/**
 * Applies [transform] to the encapsulated value if [success][kotlin.Result.isSuccess], returning the
 * [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] produced by [transform].
 *
 * Failures pass through with the original [Throwable] unchanged.
 *
 * **Exceptions from [transform] are NOT caught** — they propagate directly. Embed a
 * [result][nz.adjmunro.outcome.result.result] or [resultOf][nz.adjmunro.outcome.result.resultOf] block inside
 * [transform] if you need exception catching, or use [andThen][nz.adjmunro.outcome.result.andThen] /
 * [andThenOf][nz.adjmunro.outcome.result.andThenOf] instead.
 *
 * @param In The encapsulated success input type.
 * @param Out The success value type of the [KotlinResult][nz.adjmunro.outcome.result.KotlinResult]
 *   returned by [transform].
 * @see nz.adjmunro.outcome.result.andThen
 * @see nz.adjmunro.outcome.result.andThenOf
 */
public inline fun <In, Out> KotlinResult<In>.flatMap(
    transform: (In) -> KotlinResult<Out>,
): KotlinResult<Out> {
    return fold(onSuccess = transform, onFailure = ::failure)
}

/**
 * Flattens a nested [KotlinResult][nz.adjmunro.outcome.result.KotlinResult].
 *
 * - If [success][kotlin.Result.isSuccess], returns the inner [KotlinResult][nz.adjmunro.outcome.result.KotlinResult]
 *   (which may itself be a success or failure).
 * - If [failure][kotlin.Result.isFailure], propagates the outer [Throwable].
 */
public inline fun <T> KotlinResult<KotlinResult<T>>.flatten(): KotlinResult<T> {
    return fold(onSuccess = ::itself, onFailure = ::failure)
}

/**
 * Collects a [Collection] of [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] values into a single result.
 *
 * - ***All success:*** returns [Result.success][kotlin.Result.success] wrapping a [List] of each encapsulated value.
 * - ***Any failure:*** returns [Result.failure][kotlin.Result.failure] with the [Throwable] produced by [reduce],
 *   applied to all failure exceptions.
 *
 * @param reduce Combines all collected [Throwable] failures into a single [Throwable] to encapsulate.
 */
public inline fun <T> Collection<KotlinResult<T>>.collect(
    reduce: (List<Throwable>) -> Throwable,
): KotlinResult<List<T>> {
    contract { callsInPlace(lambda = reduce, kind = InvocationKind.AT_MOST_ONCE) }

    val (errors: List<KotlinResult<T>>, successes: List<KotlinResult<T>>) = partition { it.isFailure }

    return when {
        errors.isNotEmpty() -> kotlinFailure(throwable = reduce(errors.map { it.exceptionOrThrow() }))
        else -> kotlinSuccess(value = successes.map { it.getOrThrow() })
    }
}
