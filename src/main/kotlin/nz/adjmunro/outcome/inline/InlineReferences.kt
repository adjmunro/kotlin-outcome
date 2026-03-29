@file:Suppress(names = ["NOTHING_TO_INLINE", "unused"])

package nz.adjmunro.outcome.inline

/**
 * Syntax-sugar for a lambda that returns `this`.
 *
 * ```kotlin
 * fun <T> T.map(transform: T.() -> T): T
 *
 * // Before:
 * map(transform = { this })
 *
 * // After:
 * map(transform = ::caller)
 * ```
 *
 * @return The receiver of the lambda.
 */
@PublishedApi
internal inline fun <T> T.caller(ignore: Any? = null): T = this@caller

/**
 * Syntax-sugar for a lambda that returns `it`.
 *
 * ```kotlin
 * fun <T> T.map(transform: (T) -> T): T
 *
 * // Before:
 * map(transform = { it })
 *
 * // After:
 * map(transform = ::itself)
 * ```
 *
 * @return The first argument passed to the lambda.
 */
@PublishedApi
internal inline fun <T> itself(value: T): T = value

/**
 * Syntax-sugar for a lambda that returns `null`.
 *
 * ```kotlin
 * fun <T> T.map(transform: (T) -> T?): T?
 *
 * // Before:
 * map(transform = { null })
 *
 * // After:
 * map(transform = ::nulls)
 * ```
 *
 * @return `null`.
 */
@PublishedApi
internal inline fun nulls(ignore: Any? = null): Nothing? = null

/**
 * Syntax-sugar for a lambda that throws `it` (provided `it` is a [Throwable]).
 *
 * ```kotlin
 * fun <T> T.map(transform: (T) -> T): T
 *
 * // Before:
 * map(transform = { throw it })
 *
 * // After:
 * map(transform = ::rethrow)
 * ```
 *
 * @throws [Throwable] passed as the first argument to the lambda.
 */
@PublishedApi
internal inline fun rethrow(throwable: Throwable): Nothing = throw throwable

/**
 * Syntax-sugar for a lambda that returns [Unit].
 *
 * ```kotlin
 * fun <T> T.map(transform: (T) -> Unit): Unit
 *
 * // Before:
 * map(transform = { /* do nothing */  })
 *
 * // After:
 * map(transform = ::unit)
 * ```
 *
 * @return [Unit].
 */
@PublishedApi
internal inline fun unit(ignore: Any? = null): Unit = Unit
