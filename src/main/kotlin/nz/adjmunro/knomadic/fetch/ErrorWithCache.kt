package nz.adjmunro.knomadic.fetch

/**
 * A utility class that can be used in tandem with [Fetch] to provide and error with some cached data.
 */
@FetchDsl
public data class ErrorWithCache<out Ok : Any, out Error : Any>(
    public val error: Error,
    public val cache: Ok? = null,
) {
    public val hasCache: Boolean
        get() = cache != null
}

/**
 * Transforms the encapsulated [error][ErrorWithCache.error] and [cache][ErrorWithCache.cache].
 *
 * @param onCache Transforms the [cache][ErrorWithCache.cache] from nullable [CacheIn] to [CacheOut].
 * @param onError Transforms the [error][ErrorWithCache.error] from [ErrorIn] to [ErrorOut].
 * @return A new [ErrorWithCache] instance of type [ErrorOut] and [CacheOut].
 *
 * @see ErrorWithCache.mapCache
 * @see ErrorWithCache.mapError
 */
@FetchDsl
public inline fun <CacheIn, CacheOut, ErrorIn, ErrorOut> ErrorWithCache<CacheIn, ErrorIn>.map(
    onCache: (CacheIn?) -> CacheOut,
    onError: (ErrorIn) -> ErrorOut,
): ErrorWithCache<CacheOut, ErrorOut> where CacheIn : Any, CacheOut : Any, ErrorIn : Any, ErrorOut : Any {
    return ErrorWithCache(error = onError(error), cache = onCache(cache))
}

/**
 * Transforms the encapsulated [cache][ErrorWithCache.cache].
 *
 * @param transform Transforms the [cache][ErrorWithCache.cache] from nullable [CacheIn] to [CacheOut].
 * @return A new [ErrorWithCache] instance of type [Error] and [CacheOut].
 *
 * @see ErrorWithCache.map
 * @see ErrorWithCache.mapError
 */
@FetchDsl
public inline fun <CacheIn, CacheOut, Error> ErrorWithCache<CacheIn, Error>.mapCache(
    transform: (CacheIn?) -> CacheOut,
): ErrorWithCache<CacheOut, Error> where CacheIn : Any, CacheOut : Any, Error : Any {
    return ErrorWithCache(error = error, cache = transform(cache))
}

/**
 * Transforms the encapsulated [error][ErrorWithCache.error].
 *
 * @param transform Transforms the [error][ErrorWithCache.error] from [ErrorIn] to [ErrorOut].
 * @return A new [ErrorWithCache] instance of type [ErrorOut] and [Cache].
 *
 * @see ErrorWithCache.map
 * @see ErrorWithCache.mapCache
 */
@FetchDsl
public inline fun <Cache, ErrorIn, ErrorOut> ErrorWithCache<Cache, ErrorIn>.mapError(
    transform: (ErrorIn) -> ErrorOut,
): ErrorWithCache<Cache, ErrorOut> where Cache : Any, ErrorIn : Any, ErrorOut : Any {
    return ErrorWithCache(error = transform(error), cache = cache)
}
