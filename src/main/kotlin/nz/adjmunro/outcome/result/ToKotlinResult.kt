package nz.adjmunro.outcome.result

import kotlinx.coroutines.Deferred
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.members.rfold
import nz.adjmunro.outcome.throwable.asThrowable

/**
 * Wraps this value as a [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] using
 * [result][nz.adjmunro.outcome.result.result].
 *
 * - This value is wrapped as a [Result.success][kotlin.Result.success].
 * - Thrown [Exceptions][Exception] are wrapped as a [Result.failure][kotlin.Result.failure].
 * - [CancellationException][kotlinx.coroutines.CancellationException] is caught (non-suspend property).
 *   ***Do not use in suspend contexts*** — use [toKotlinResult][nz.adjmunro.outcome.result.toKotlinResult] instead.
 * - [Errors][Error] are always propagated.
 *
 * @receiver The value to wrap.
 */
@OptIn(UnsafeForCoroutineCancellation::class)
public inline val <T> T.asKotlinResult: KotlinResult<T>
    get() = result { this@asKotlinResult }

/**
 * Wraps this value directly as a [KotlinResult][nz.adjmunro.outcome.result.KotlinResult]
 * [success][kotlin.Result.isSuccess]. No exception catching.
 */
public inline val <T> T.asKotlinSuccess: KotlinResult<T>
    get() = kotlinSuccess(value = this)

/**
 * Wraps this [Throwable] directly as a [KotlinResult][nz.adjmunro.outcome.result.KotlinResult]
 * [failure][kotlin.Result.isFailure]. No exception catching.
 */
public inline val Throwable.asKotlinFailure: KotlinResult<*>
    get() = kotlinFailure(throwable = this)

/**
 * Wraps this value as a [KotlinResult][nz.adjmunro.outcome.result.KotlinResult]
 * [failure][kotlin.Result.isFailure] by converting it to a [Throwable] first. No exception catching.
 */
public inline val <T> T.asKotlinFailure: KotlinResult<*>
    get() = kotlinFailure { asThrowable() }

/**
 * Converts an [Outcome][nz.adjmunro.outcome.Outcome] to a [KotlinResult][nz.adjmunro.outcome.result.KotlinResult].
 *
 * Success values are wrapped with [kotlinSuccess][nz.adjmunro.outcome.result.kotlinSuccess].
 * Failure values are converted to [Throwable] and wrapped with
 * [kotlinFailure][nz.adjmunro.outcome.result.kotlinFailure].
 */
public inline val <Ok, Error> Outcome<Ok, Error>.toKotlinResult: KotlinResult<Ok>
    get() = rfold(success = { kotlinSuccess(value = value) }) {
        kotlinFailure(
            throwable = error.asThrowable {
                "Outcome error converted to KotlinResult failure: $it"
            },
        )
    }

/**
 * Wraps this value as a [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] based on [predicate].
 *
 * - If [predicate] returns `true`, wraps as [Result.success][kotlin.Result.success].
 * - If [predicate] returns `false`, calls [failure] to produce a [Throwable] and wraps as
 *   [Result.failure][kotlin.Result.failure].
 *
 * By default, any value that is not a [Throwable] is treated as a success.
 *
 * @param T The type of the receiver value.
 * @param predicate Determines whether the value represents a success. Defaults to `{ this !is Throwable }`.
 * @param failure Produces the [Throwable] when [predicate] returns `false`.
 */
public inline fun <T> T.toKotlinResult(
    predicate: T.() -> Boolean = { this !is Throwable },
    failure: (T) -> Throwable = { value: T ->
        value.asThrowable { "${it}.asKotlinResult() failed predicate test!" }
    },
): KotlinResult<T> {
    return when (predicate()) {
        true -> kotlinSuccess(value = this@toKotlinResult)
        false -> kotlinFailure(throwable = failure(this))
    }
}

/**
 * Awaits the [Deferred][kotlinx.coroutines.Deferred] and returns its result as a
 * [KotlinResult][nz.adjmunro.outcome.result.KotlinResult].
 *
 * - On success: wrapped as [Result.success][kotlin.Result.success].
 * - [Exception] thrown by [await][kotlinx.coroutines.Deferred.await]: wrapped as
 *   [Result.failure][kotlin.Result.failure].
 * - [CancellationException][kotlinx.coroutines.CancellationException]: re-thrown (preserves structured concurrency).
 * - [Error]: always propagated.
 *
 * @receiver The [Deferred][kotlinx.coroutines.Deferred] to await.
 */
public suspend fun <T> Deferred<T>.awaitKotlinResult(): KotlinResult<T> {
    return resultOf { await() }
}
