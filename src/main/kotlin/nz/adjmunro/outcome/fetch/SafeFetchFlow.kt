package nz.adjmunro.outcome.fetch

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.AbstractFlow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withTimeout
import nz.adjmunro.outcome.util.nonFatalOrThrow
import kotlin.time.Duration

/**
 * [AbstractFlow] implementation for [FetchFlow].
 *
 * *Use [fetch] to provide an instance of this class.*
 * ```kotlin
 * // Typealias for Flow<Fetch<Int>>
 * val a: FetchFlow<Int> = fetch { 4 }
 *
 * // Will throw TimeoutCancellationException if [block] is not emitted within 4 seconds
 * val b: FetchFlow<Int> = fetch(timeout = 4.seconds) { 4 }
 *
 * // The recover param can map unexpected errors to a Fetch state.
 * val c: FetchFlow<Int> = fetch(timeout = 4.seconds, recover = { Prefetch }) { 4 }
 * ```
 * - [Prefetch] is used for initial states, and is not emitted by the [FetchFlow].
 * - [Fetching] is emitted automatically *before* [block] is executed.
 * - [Finished] automatically encapsulates the result of [block].
 *
 * @property T The type of the result of the fetch.
 * @param timeout The duration to wait [withTimeout] for the fetch [block] to complete once.
 * @param recover The transformation to apply to any [non-fatal][nonFatalOrThrow] [Throwable] that is caught.
 * @param block The block of code to execute.
 * @return [FetchFlow] -- a [Flow] that encapsulates the [Fetch] behaviour.
 * @see fetch
 */
@OptIn(ExperimentalCoroutinesApi::class) @FetchDsl
public class SafeFetchFlow<T : Any> @PublishedApi internal constructor(
    private val timeout: Duration = Duration.INFINITE,
    private val recover: FetchCollector<T>.(Throwable) -> Fetch<T> = { throw it },
    private val block: suspend FetchCollector<T>.() -> T,
) : AbstractFlow<Fetch<T>>(), FetchFlow<T> {
    override suspend fun collectSafely(collector: FetchCollector<T>) {
        // Automatic `fetching` status
        collector.fetching()

        // Execute the block & await the result
        collector.emit(recover = recover) {
            withTimeout(timeout = timeout) { Finished(result = block(collector)) }
        }
    }
}
