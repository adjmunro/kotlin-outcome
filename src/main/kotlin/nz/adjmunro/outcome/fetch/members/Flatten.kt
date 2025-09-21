package nz.adjmunro.outcome.fetch.members

import nz.adjmunro.inline.itself
import nz.adjmunro.outcome.fetch.Fetch
import nz.adjmunro.outcome.fetch.FetchDsl
import nz.adjmunro.outcome.fetch.Fetching
import nz.adjmunro.outcome.fetch.Finished

/**
 * Flatten a nested [Fetch] into a single [Fetch].
 */
@FetchDsl
public inline val <T : Any> Fetch<Fetch<T>>.flatten: Fetch<T>
    get() = fold(
        prefetch = ::itself,
        fetching = { cache ?: Fetching() },
        finished = Finished<Fetch<T>>::result,
    )
