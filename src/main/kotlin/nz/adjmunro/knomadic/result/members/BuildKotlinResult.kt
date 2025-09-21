package nz.adjmunro.knomadic.result.members

import kotlinx.coroutines.CancellationException
import nz.adjmunro.knomadic.result.KotlinResult
import nz.adjmunro.knomadic.result.KotlinResultDsl
import nz.adjmunro.knomadic.util.isFatal
import nz.adjmunro.knomadic.util.nonFatalOrThrow
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Context runner to encapsulate the result of [block] as a [nz.adjmunro.knomadic.result.KotlinResult].
 *
 * - If [block] throws a [non-fatal][Throwable.isFatal] exception, the [Throwable] is encapsulated as a [Result.failure].
 * - If [block] throws a **[fatal][Throwable.isFatal]** exception, the [Throwable] is re-thrown!
 *
 * > For example, [CancellationException], which is necessary for Kotlin's
 * > structured concurrency model, is considered fatal and will always be rethrown.
 * > [Read more about issues with Kotlin's `runCatching()`](https://github.com/Kotlin/kotlinx.coroutines/issues/1814)
 *
 * @param T The return type of [block].
 * @param block The protected try-block which may throw and exception.
 * @throws Throwable See [Throwable.isFatal] for list of unsafe exceptions.
 * @see Throwable.nonFatalOrThrow
 */
@KotlinResultDsl
public inline fun <T> resultOf(block: () -> T): KotlinResult<T> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }

    return try { success(value = block()) } catch (e: Throwable) {
        failure(exception = e.nonFatalOrThrow())
    }
}

/**
 * Context runner to encapsulate the result of [block] as a [KotlinResult].
 *
 * - If [block] throws a [non-fatal][Throwable.isFatal] exception, the [Throwable] is encapsulated as a [Result.failure].
 * - If [block] throws a **[fatal][Throwable.isFatal]** exception, the [Throwable] is re-thrown!
 *
 * > For example, [CancellationException], which is necessary for Kotlin's
 * > structured concurrency model, is considered fatal and will always be rethrown.
 * > [Read more about issues with Kotlin's `runCatching()`](https://github.com/Kotlin/kotlinx.coroutines/issues/1814)
 *
 * @param In The receiver type of [block].
 * @param Out The return type of [block].
 * @param block The protected try-block which may throw and exception.
 * @throws Throwable See [Throwable.isFatal] for list of unsafe exceptions.
 * @see Throwable.nonFatalOrThrow
 */
@KotlinResultDsl
public inline fun <In, Out> In.resultOf(block: In.() -> Out): KotlinResult<Out> {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }

    return try { success(value = block(this@resultOf)) } catch (e: Throwable) {
        failure(exception = e.nonFatalOrThrow())
    }
}

/**
 * Context runner that catches and normalises all [non-fatal][Throwable.isFatal]
 * thrown exceptions into a `null` value.
 *
 * ```kotlin
 * // Example Usage:
 * val a: String? = nullable { throw Exception() } // == null
 * val b: String? = nullable { "Hello, World!" } // == "Hello, World!"
 * ```
 *
 * @param T The return type of [block].
 * @param block The protected try-block which may throw an exception.
 * @return The result of [block] or `null` if an exception was thrown.
 */
@KotlinResultDsl
public inline fun <T> nullable(block: () -> T): T? {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }

    return try { block() } catch (e: Throwable) {
        if (e.isFatal()) throw e else null
    }
}

/**
 * Context runner that catches and normalises all [non-fatal][Throwable.isFatal]
 * thrown exceptions into a `null` value.
 *
 * ```kotlin
 * // Example Usage:
 * val a: String? = 0.nullable { throw Exception() } // == null
 * val b: String? = 0.nullable { toString() } // == "0"
 * ```
 *
 * @param In The receiver type of [block].
 * @param Out The return type of [block].
 * @param block The protected try-block which may throw an exception.
 * @return The result of [block] or `null` if an exception was thrown.
 */
@KotlinResultDsl
public inline fun <In, Out> In.nullable(block: In.() -> Out): Out? {
    contract { callsInPlace(block, InvocationKind.AT_MOST_ONCE) }

    return try { block(this@nullable) } catch (e: Throwable) {
        if (e.isFatal()) throw e else null
    }
}
