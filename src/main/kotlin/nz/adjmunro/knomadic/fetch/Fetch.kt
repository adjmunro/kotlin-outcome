package nz.adjmunro.knomadic.fetch

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import kotlinx.coroutines.withTimeout
import nz.adjmunro.knomadic.FetchCollector
import nz.adjmunro.knomadic.FetchFlow
import kotlin.time.Duration

/**
 * Create a new [FetchFlow] with the given [block] of code to execute.
 *
 * - [Prefetch] is for default states, and is not emitted by the resulting [FetchFlow].
 * - [Fetching] is emitted automatically *before* [block] is executed.
 * - [Finished] automatically encapsulates the result of [block].
 *
 * However, you can manually [emit][FlowCollector.emit] these statuses via
 * [emit][FlowCollector.emit], [prefetch], [fetching], and [finished].
 *
 * @param T The type of the result of the fetch.
 * @param timeout The duration to wait [withTimeout] for the fetch [block] to complete once.
 * @param recover The transformation to apply to any [non-fatal][nz.adjmunro.knomadic.util.nonFatalOrThrow] [Throwable] that is caught.
 * @param block The block of code to execute.
 * @return [FetchFlow] -- a [Flow] that encapsulates the [Fetch] behaviour.
 * @see SafeFetchFlow
 */
@FetchDsl
public fun <T : Any> fetch(
    timeout: Duration = Duration.INFINITE,
    recover: FetchCollector<T>.(Throwable) -> Fetch<T> = { throw it },
    block: suspend FetchCollector<T>.() -> T,
): FetchFlow<T> = SafeFetchFlow(
    timeout = timeout,
    recover = recover,
    block = block,
)

/**
 * Encapsulates a reactive asynchronous future, such as.
 *
 * - [Prefetch] is for default states, and is not generally emitted;
 * - [Fetching] is emitted automatically *before* the fetch operation is executed when using [fetch];
 * - [Finished] automatically encapsulates the result of the fetch operation.
 *
 * @see fetch
 * @see FetchFlow
 */
public sealed interface Fetch<out T : Any>

/**
 * A fetch operation that has not yet been started.
 *
 * *This is for default states, and is not generally emitted.*
 */
@FetchDsl
public data object Prefetch : Fetch<Nothing>


/**
 * A fetch operation that is currently in progress.
 *
 * *This is emitted automatically before the fetch operation is executed when using [fetch].*
 */
@FetchDsl
@JvmInline
public value class Fetching<out T : Any>(public val cache: T? = null) : Fetch<T> {
    public operator fun component1(): T? = cache
    override fun toString(): String {
        return when (cache) {
            null -> "Fetching(cache = null)"
            else -> "Fetching<${cache::class.simpleName}>(cache = $cache)"
        }
    }
}

/**
 * A [fetch] operation that has finished.
 *
 * *Following the single-responsibility principle, the success or failure of the [fetch] is left
 * up to the encapsulated type, [T]. It is recommended to use an
 * [Outcome][nz.adjmunro.knomadic.outcome.Outcome] for this purpose.*
 *
 * @param result The [Finished] result of this [fetch] operation.
 */
@FetchDsl
@JvmInline
public value class Finished<out T : Any>(public val result: T) : Fetch<T> {
    public operator fun component1(): T = result
    override fun toString(): String {
        return "FetchFinished<${result::class.simpleName}>(result = $result)"
    }
}
