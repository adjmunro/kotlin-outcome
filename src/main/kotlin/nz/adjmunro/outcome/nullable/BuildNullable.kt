package nz.adjmunro.outcome.nullable

import nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * `Suspend` runner that catches and normalizes **most thrown [exceptions][Exception]** into a `null` value.
 *
 * - ***Will NOT catch [CancellationException][kotlinx.coroutines.CancellationException] if the coroutine
 *   is [no longer active][kotlinx.coroutines.ensureActive]!***
 * - ***Does NOT catch [Error] subclasses.** Errors should be fatal.*
 *
 * ```kotlin
 * val a: String? = nullableOf { throw Exception() } // == null
 * val b: String? = nullableOf { "Hello, World!" }   // == "Hello, World!"
 * ```
 *
 * Shorthand for `try { block() } catch (_: Exception) { null }` with an
 * [ensureActive][kotlinx.coroutines.ensureActive] guard on [CancellationException][kotlinx.coroutines.CancellationException].
 *
 * @param T The return type of [block].
 * @param block The protected try-block which may throw an [Exception].
 * @return The result of [block], or `null` if an [Exception] was thrown.
 * @throws Error Always rethrown — [Error] subclasses are not caught.
 * @throws kotlinx.coroutines.CancellationException If the coroutine has been cancelled.
 * @see nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
 * @see nz.adjmunro.outcome.nullable.nullable
 */
@EnsuresActiveCoroutine
public suspend inline fun <T> nullableOf(block: () -> T): T? {
    contract { callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE) }
    return try { block() } catch (e: Exception) {
        if (e is CancellationException) currentCoroutineContext().ensureActive()
        null
    }
}

/**
 * Runner that catches and normalizes **all thrown [exceptions][Exception]** into a `null` value.
 *
 * - ***Will catch [CancellationException][kotlinx.coroutines.CancellationException]!**
 *   In a `suspend` context, use [nullableOf][nz.adjmunro.outcome.nullable.nullableOf] instead.*
 * - ***Does NOT catch [Error] subclasses.** Errors should be fatal.*
 *
 * ```kotlin
 * val a: String? = nullable { throw Exception() } // == null
 * val b: String? = nullable { "Hello, World!" }   // == "Hello, World!"
 * ```
 *
 * Shorthand for `try { block() } catch (_: Exception) { null }`.
 *
 * @param T The return type of [block].
 * @param block The protected try-block which may throw an [Exception].
 * @return The result of [block], or `null` if an [Exception] was thrown.
 * @throws Error Always rethrown — [Error] subclasses are not caught.
 * @see nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
 * @see nz.adjmunro.outcome.nullable.nullableOf
 */
@UnsafeForCoroutineCancellation
public inline fun <T> nullable(block: () -> T): T? {
    contract { callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE) }
    return try { block() } catch (_: Exception) { null }
}
