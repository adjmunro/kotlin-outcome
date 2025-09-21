package nz.adjmunro.outcome.fetch.members

import nz.adjmunro.inline.nulls
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished
import nz.adjmunro.outcome.fetch.Prefetch

/**
 * Unwrap the internal value of a [Fetch], or [default].
 * - [Prefetch] returns [default];
 * - [Fetching] returns [cache] (or [default] if `null`);
 * - [Finished] returns [result].
 *
 * @return A value of type [T].
 */
@FetchDsl
public infix fun <T: Any> Fetch<T>.getOrDefault(default: T): T {
    return fold(prefetch = { default }, fetching = { cache ?: default }, finished = Finished<T>::result)
}

/**
 * Unwrap the internal value of a [Fetch], or [recover].
 * - [Prefetch] returns the result of [recover];
 * - [Fetching] returns [cache] (or [recover] if `null`);
 * - [Finished] returns [result].
 *
 * @return A value of type [T].
 */
@FetchDsl
public inline fun <T> Fetch<T & Any>.getOrElse(recover: (Fetch<T & Any>) -> T): T {
    return fold(prefetch = recover, fetching = { cache ?: recover(this) }, finished = Finished<T & Any>::result)
}

/**
 * Unwrap the internal value of a [Fetch], or `null`.
 * - [Prefetch] returns `null`;
 * - [Fetching] returns [cache] (or null);
 * - [Finished] returns [result].
 *
 * @return A value of type [T], or `null`.
 */
@FetchDsl
public fun <T: Any> Fetch<T>.getOrNull(): T? {
    return fold(prefetch = ::nulls, fetching = Fetching<T>::cache, finished = Finished<T>::result)
}

/**
 * Unwrap the internal value of a [Fetch], or [throw][IllegalStateException].
 *
 * - [Prefetch] throws an [IllegalStateException];
 * - [Fetching] returns [cache] (or [throws][IllegalStateException]);
 * - [Finished] returns [result].
 *
 * @return A value of type [T].
 * @throws IllegalStateException if the [Fetch] is a [Prefetch] or [Fetching.cache] is `null`.
 */
@FetchDsl
public fun <T: Any> Fetch<T>.getOrThrow(): T {
    return fold(
        prefetch = { error("Fetch has not started!") },
        fetching = { cache ?: error("Fetch has not finished! (no result or cache)") },
        finished = Finished<T>::result,
    )
}
