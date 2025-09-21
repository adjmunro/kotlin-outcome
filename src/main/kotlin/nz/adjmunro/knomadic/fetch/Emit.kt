package nz.adjmunro.knomadic.fetch

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector
import nz.adjmunro.knomadic.FetchCollector
import nz.adjmunro.knomadic.util.nonFatalOrThrow

/**
 * [Send][FlowCollector.emit] a [fetch not started][Prefetch]
 * status to the current [flow-scope][Flow].
 *
 * @see emit
 * @see FetchCollector.fetching
 * @see FetchCollector.finished
 */
@FetchDsl
public suspend inline fun FetchCollector<Nothing>.prefetch() {
    emit(value = Prefetch)
}

/**
 * [Send][FlowCollector.emit] a [fetch in progress][Fetching]
 * status to the current [flow-scope][Flow].
 *
 * @see emit
 * @see FetchCollector.prefetch
 * @see FetchCollector.finished
 */
@FetchDsl
public suspend inline fun <T: Any> FetchCollector<T>.fetching(cache: T? = null) {
    emit(value = Fetching(cache = cache))
}

/**
 * [Send][FlowCollector.emit] a [fetch finished][Finished] status to the current [flow-scope][Flow],
 * with the encapsulated [result].
 *
 * @see emit
 * @see FetchCollector.prefetch
 * @see FetchCollector.fetching
 */
@FetchDsl
public suspend inline fun <T : Any> FetchCollector<T>.finished(result: T) {
    emit(value = Finished(result = result))
}

/**
 * [Emit][FlowCollector.emit] the result of [block], with a built-in [try-catch][recover].
 *
 * > ***By default, [recover] rethrows!***
 *
 * @param recover The transformation to apply to all caught [non-fatal][nonFatalOrThrow] throwables.
 * @param block The block of code to execute.
 */
@FetchDsl
public suspend inline fun <T> FlowCollector<T>.emit(
    recover: FlowCollector<T>.(Throwable) -> T = { throw it },
    block: FlowCollector<T>.() -> T,
) {
    try {
        emit(value = block())
    } catch (e: Throwable) {
        emit(value = recover(e.nonFatalOrThrow()))
    }
}
