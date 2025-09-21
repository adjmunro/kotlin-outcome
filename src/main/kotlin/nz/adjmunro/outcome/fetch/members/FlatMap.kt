package nz.adjmunro.outcome.fetch.members

import nz.adjmunro.inline.caller
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished
import nz.adjmunro.outcome.fetch.Prefetch

/**
 * Flatmap a [Prefetch] state.
 *
 * This is a convenience method for [fold][Fetch.fold], that only transforms
 * one state and forces the output to be a [Fetch] variant.
 *
 * *If [In] != [Out], then [Out] = the closest common ancestor of [In] and [Out].*
 *
 * @receiver The [Fetch] to transform.
 * @param In The type of the [Fetch] value.
 * @param Out The type of the [transformed][transform] value.
 * @param transform The lambda to transform the [Prefetch] state.
 * @return The maybe transformed [Fetch], now of type [Out].
 */
@FetchDsl
public inline fun <In: Out, Out: Any> Fetch<In>.flatMapPrefetch(
    transform: Prefetch.() -> Fetch<Out>,
): Fetch<Out> = fold(prefetch = transform, fetching = ::caller, finished = ::caller)

/**
 * Flatmap a [Fetching] state.
 *
 * This is a convenience method for [fold][Fetch.fold], that only transforms
 * one state and forces the output to be a [Fetch] variant.
 *
 * *If [In] != [Out], then [Out] = the closest common ancestor of [In] and [Out].*
 *
 * @receiver The [Fetch] to transform.
 * @param In The type of the [Fetch] value.
 * @param Out The type of the [transformed][transform] value.
 * @param transform The lambda to transform the [Fetching] state.
 * @return The maybe transformed [Fetch], now of type [Out].
 */
@FetchDsl
public inline fun <In: Out, Out: Any> Fetch<In>.flatMapFetching(
    transform: Fetching<In>.() -> Fetch<Out>,
): Fetch<Out> = fold(prefetch = ::caller, fetching = transform, finished = ::caller)

/**
 * Flatmap a [Finished] state.
 *
 * This is a convenience method for [fold][Fetch.fold], that only transforms
 * one state and forces the output to be a [Fetch] variant.
 *
 * *If [In] != [Out], then [Out] = the closest common ancestor of [In] and [Out].*
 *
 * @receiver The [Fetch] to transform.
 * @param In The type of the [Fetch] value.
 * @param Out The type of the [transformed][transform] value.
 * @param transform The lambda to transform the [Finished] state.
 * @return The maybe transformed [Fetch], now of type [Out].
 */
@FetchDsl
public inline fun <In: Out, Out: Any> Fetch<In>.flatMapFinished(
    transform: Finished<In>.() -> Fetch<Out>,
): Fetch<Out> = fold(prefetch = ::caller, fetching = ::caller, finished = transform)
