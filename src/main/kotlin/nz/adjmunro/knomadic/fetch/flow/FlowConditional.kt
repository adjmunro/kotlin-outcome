package nz.adjmunro.knomadic.fetch.flow

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.onEach
import nz.adjmunro.knomadic.FetchFlow
import nz.adjmunro.knomadic.fetch.Fetch
import nz.adjmunro.knomadic.fetch.FetchDsl
import nz.adjmunro.knomadic.fetch.Fetching
import nz.adjmunro.knomadic.fetch.Finished
import nz.adjmunro.knomadic.fetch.Prefetch
import nz.adjmunro.knomadic.fetch.members.fold
import nz.adjmunro.knomadic.fetch.members.getOrNull
import nz.adjmunro.knomadic.fetch.members.isNotEmpty

/**
 * Filter a [FetchFlow] by [isNotEmpty], and unwrap the value if it exists.
 *
 * @receiver The [FetchFlow] to filter.
 * @param T The type of the [Fetch] value.
 * @return A [Flow] of [T].
 */
@FetchDsl
public fun <T : Any> FetchFlow<T>.filterIsNotEmpty(): Flow<T> = mapNotNull { it.getOrNull() }

/**
 * [On each][Flow.onEach] emission, execute a lambda according to [Fetch] state.
 *
 * @receiver The [FetchFlow] to perform actions on.
 * @param T The type of the [Fetch] value.
 * @param prefetch Action when emission contains [Prefetch].
 * @param fetching Action when emission contains [Fetching].
 * @param finished Action when emission contains [Finished].
 * @return The original [FetchFlow].
 */
@FetchDsl
public inline fun <T : Any> FetchFlow<T>.onEach(
    crossinline prefetch: suspend Prefetch.() -> Unit = {},
    crossinline fetching: suspend Fetching<T>.() -> Unit = {},
    crossinline finished: suspend Finished<T>.() -> Unit = {},
): FetchFlow<T> = onEach { fetch: Fetch<T> ->
    fetch.fold(prefetch = { prefetch() }, fetching = { fetching() }, finished = { finished() })
}
