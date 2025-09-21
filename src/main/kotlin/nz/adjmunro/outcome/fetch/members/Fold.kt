package nz.adjmunro.outcome.fetch.members

import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished
import nz.adjmunro.outcome.fetch.Prefetch
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Transform all [Fetch] status into the [Output] type.
 *
 * *Fold can be used to `map` or `flatMap` all [Fetch] variants,
 * but it also can output any type.*
 * 
 * @receiver The [Fetch] to fold.
 * @param Data The type of the [Fetch] value.
 * @param Output The type of the folded value.
 * @param prefetch The lambda to transform the [Prefetch] status.
 * @param fetching The lambda to transform the [Fetching] status.
 * @param finished The lambda to transform the [Finished] status.
 * @return The folded value, of type [Output].
 */
@FetchDsl
public inline fun <Data : Any, Output> Fetch<Data>.fold(
    prefetch: Prefetch.() -> Output,
    fetching: Fetching<Data>.() -> Output,
    finished: Finished<Data>.() -> Output,
): Output {
    contract { 
        callsInPlace(prefetch, InvocationKind.AT_MOST_ONCE)
        callsInPlace(fetching, InvocationKind.AT_MOST_ONCE)
        callsInPlace(finished, InvocationKind.AT_MOST_ONCE)
    }
    
    return when (this@fold) {
        is Prefetch -> prefetch()
        is Fetching<Data> -> fetching()
        is Finished<Data> -> finished()
    }
}
