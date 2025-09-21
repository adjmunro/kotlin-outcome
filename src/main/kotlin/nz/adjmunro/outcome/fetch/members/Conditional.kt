package nz.adjmunro.outcome.fetch.members

import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished
import nz.adjmunro.outcome.fetch.Prefetch
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

/** @return `true` if [Fetch] *is not* [Prefetch]. */
@FetchDsl
public inline val <T : Any> Fetch<T>.isStarted: Boolean
    get() = !isPrefetch()

/** @return `true` if [Fetch] *is not* [Fetching]. */
@FetchDsl
public inline val <T : Any> Fetch<T>.isNotFetching: Boolean
    get() = !isFetching()

/** @return `true` if [Fetch] *is not* [Finished]. */
@FetchDsl
public inline val <T : Any> Fetch<T>.isNotFinished: Boolean
    get() = !isFinished()

/** @return `true` if [Prefetch] or [Fetching.cache] is `null` */
@FetchDsl
public inline val <T: Any> Fetch<T>.isEmpty: Boolean
    get() = isPrefetch() || isFetching { cache == null }

/** @return `true` if [Finished] or [Fetching.cache] is not `null` */
@FetchDsl
public inline val <T: Any> Fetch<T>.isNotEmpty: Boolean
    get() = isFinished() || isFetching { cache != null }

/** @return `true` if [Fetch] is [Prefetch]. */
@FetchDsl
public fun <T : Any> Fetch<T>.isPrefetch(): Boolean {
    contract { returns(true) implies(this@isPrefetch is Prefetch) }
    return this is Prefetch
}

/** @return `true` if [Fetch] is [Fetching]. */
@FetchDsl
public fun <T : Any> Fetch<T>.isFetching(): Boolean {
    contract { returns(true) implies(this@isFetching is Fetching) }
    return this is Fetching
}

/** @return `true` if [predicate] is `true` *and* [Fetch] is [Fetching]. */
@FetchDsl
public inline fun <T : Any> Fetch<T>.isFetching(predicate: Fetching<T>.() -> Boolean): Boolean {
    contract {
        returns(true) implies (this@isFetching is Fetching<T>)
        callsInPlace(predicate, AT_MOST_ONCE)
    }
    return isFetching() && predicate()
}

/** @return `true` if [Fetch] is [Finished]. */
@FetchDsl
public fun <T : Any> Fetch<T>.isFinished(): Boolean {
    contract { returns(true) implies (this@isFinished is Finished<T>) }
    return this@isFinished is Finished<T>
}

/** @return `true` if [predicate] is `true` *and* [Fetch] is [Finished]. */
@FetchDsl
public inline fun <T : Any> Fetch<T>.isFinished(predicate: Finished<T>.() -> Boolean): Boolean {
    contract {
        returns(true) implies (this@isFinished is Finished<T>)
        callsInPlace(predicate, AT_MOST_ONCE)
    }
    return isFinished() && predicate()
}

/** [Also][also] do an [action], if [Fetch] is [Prefetch]. */
@FetchDsl
public inline fun <T : Any> Fetch<T>.onPrefetch(
    action: Prefetch.() -> Unit,
): Fetch<T> {
    contract { callsInPlace(action, AT_MOST_ONCE) }
    if (isPrefetch()) action()
    return this
}

/** [Also][also] do an [action], if [Fetch] is [Fetching]. */
@FetchDsl
public inline fun <T : Any> Fetch<T>.onFetching(
    action: Fetching<T>.() -> Unit,
): Fetch<T> {
    contract { callsInPlace(action, AT_MOST_ONCE) }
    if (isFetching()) action()
    return this
}

/** [Also][also] do an [action], if [Fetch] is [Finished]. */
@FetchDsl
public inline fun <T : Any> Fetch<T>.onFinished(action: Finished<T>.() -> Unit): Fetch<T> {
    contract { callsInPlace(action, AT_MOST_ONCE) }
    if (isFinished()) action()
    return this
}
