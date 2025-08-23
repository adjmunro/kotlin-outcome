package nz.adjmunro.outcome.result.members

import kotlinx.coroutines.Deferred
import nz.adjmunro.outcome.result.KotlinResult
import nz.adjmunro.outcome.result.KotlinResultDsl
import nz.adjmunro.outcome.util.asThrowable

/**
 * Wrap any [T] as a [KotlinResult] using [resultOf].
 *
 * - [Exceptions][Exception] will be wrapped as a [Result.failure].
 * - [Errors][Error] will be thrown.
 * - [CancellationException][kotlinx.coroutines.CancellationException] will trigger [ensureActive][kotlinx.coroutines.ensureActive].
 * - Otherwise, the value is wrapped as a [Result.success].
 *
 * @receiver The value to wrap as a [KotlinResult].
 * @return A [KotlinResult] containing the value as a [Result.success] or a [Result.failure].
 */
@KotlinResultDsl
public inline val <T> T.asKotlinResult: KotlinResult<T>
    get() = resultOf { this@asKotlinResult }

/**
 * Wrap any [T] as a [KotlinResult] using [resultOf].
 *
 * @param T The type of the value to wrap.
 * @param predicate A predicate to determine if the value should be wrapped as a success or failure.
 * @param failure A function to create a [Throwable] if the predicate fails.
 * @return A [KotlinResult] containing the value as a [Result.success] or a [Result.failure].
 * @see asThrowable
 */
@KotlinResultDsl
public inline fun <T> T.asKotlinResult(
    predicate: T.() -> Boolean = { this !is Throwable },
    failure: (T) -> Throwable = { asThrowable { "${it}.asKotlinResult() failed predicate test!" } },
): KotlinResult<T> {
    return when (predicate()) {
        true -> Result.success(value = this@asKotlinResult)
        false -> Result.failure(exception = failure(this@asKotlinResult))
    }
}

/**
 * Await the result of a [Deferred] as a [KotlinResult].
 *
 * - If the [Deferred] completes successfully, the value is wrapped as a [Result.success].
 * - If the [Deferred] fails with an exception or is cancelled, it is wrapped as a [Result.failure].
 *
 * @receiver The [Deferred] to await.
 * @return A [KotlinResult] containing the result of the [Deferred].
 */
@KotlinResultDsl
public suspend fun <T : Any> Deferred<T>.awaitKotlinResult(): KotlinResult<T> {
    return resultOf { await() }
}
