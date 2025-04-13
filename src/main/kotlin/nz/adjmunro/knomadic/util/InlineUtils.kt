@file:Suppress("NOTHING_TO_INLINE")

package nz.adjmunro.knomadic.util

import nz.adjmunro.knomadic.KnomadicDsl
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

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
 * map(transform = ::it)
 * ```
 *
 * @return The first argument passed to the lambda.
 */
@KnomadicDsl
public inline fun <T> itself(value: T): T = value

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
@KnomadicDsl
public inline fun <T> T.caller(ignore: Any? = null): T = this@caller

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
@KnomadicDsl
public inline fun rethrow(throwable: Throwable): Nothing = throw throwable

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
@KnomadicDsl
public inline fun nulls(ignore: Any? = null): Nothing? = null

/**
 * Syntax-sugar for a lambda folds a nullable receiver type into type [Out].
 *
 * ```kotlin
 * val result: String? = null
 *
 * // Before:
 * result?.length ?: 0
 *
 * // Before:
 * when (result) {
 *    null -> 0
 *    else -> result.length
 * }
 *
 * // After:
 * result.nullfold(none = { 0 }, some = { it.length })
 * ```
 *
 * @param In The type of the receiver.
 * @param Out The type of the return value.
 * @param none The lambda to call if the receiver is `null`.
 * @param some The lambda to call if the receiver is not `null`.
 * @return The result of the lambda passed to [some] or [none].
 */
@KnomadicDsl
public inline fun <In, Out> In.nullfold(
    @BuilderInference none: (NullPointerException) -> Out,
    @BuilderInference some: (In & Any) -> Out,
): Out {
    contract {
        callsInPlace(some, AT_MOST_ONCE)
        callsInPlace(none, AT_MOST_ONCE)
    }

    return when (this@nullfold) {
        null -> none(NullPointerException("Nullfold source was null."))
        else -> some(this@nullfold)
    }
}

/**
 * Syntax-sugar for a lambda folds a potentially [Throwable] receiver type into type [Out].
 *
 * *This function does not catch exceptions.*
 *
 * ```kotlin
 * val result: Throwable = IllegalStateException()
 *
 * // Before:
 * (result as? Throwable)?.let { "$it" } ?: "The world is good!"
 *
 * // After:
 * result.throwfold(throws = { "$it" }, pass = { "The world is good!" })
 * ```
 *
 * @param In The type of the receiver.
 * @param Out The type of the return value.
 * @param throws The lambda to call if the receiver is a [Throwable].
 * @param pass The lambda to call if the receiver is not a [Throwable].
 * @return The result of the lambda passed to [throws] or [pass].
 */
@KnomadicDsl
public inline fun <In, Out> In.throwfold(
    @BuilderInference throws: (Throwable) -> Out,
    @BuilderInference pass: (In) -> Out,
): Out {
    contract {
        callsInPlace(pass, AT_MOST_ONCE)
        callsInPlace(throws, AT_MOST_ONCE)
    }

    return when (this@throwfold) {
        is Throwable -> throws(this@throwfold)
        else -> pass(this@throwfold)
    }
}
