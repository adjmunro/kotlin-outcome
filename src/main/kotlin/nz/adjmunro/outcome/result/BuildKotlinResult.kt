package nz.adjmunro.outcome.result

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.inline.unit
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Suspend runner that catches and encapsulates [T] and **most thrown [Exceptions][Exception]** as a
 * [KotlinResult][nz.adjmunro.outcome.result.KotlinResult].
 *
 * - ***Will NOT catch [CancellationException][kotlinx.coroutines.CancellationException] if the coroutine is
 *   [no longer active][kotlinx.coroutines.ensureActive].***
 * - ***Does NOT catch [Error] subtypes.** Errors are always fatal.*
 *
 * Prefer this over [result][nz.adjmunro.outcome.result.result] in suspend contexts.
 *
 * ```kotlin
 * val a: KotlinResult<String> = resultOf { throw Exception() } // Result.failure(Exception())
 * val b: KotlinResult<String> = resultOf { "Hello, World!" }   // Result.success("Hello, World!")
 * ```
 *
 * @param T The return type of [block].
 * @param finally An optional cleanup block, always runs after [block].
 * @param block The protected block to execute. May throw an exception.
 * @return The [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] of [block].
 * @throws Error Always propagated — errors are fatal.
 * @throws kotlinx.coroutines.CancellationException If the coroutine has been cancelled.
 * @see nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
 * @see nz.adjmunro.outcome.result.result
 */
@EnsuresActiveCoroutine
public suspend inline fun <T> resultOf(
    finally: () -> Unit = ::unit,
    block: () -> T,
): KotlinResult<T> {
    contract {
        callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE)
        callsInPlace(lambda = finally, kind = InvocationKind.EXACTLY_ONCE)
    }

    return try {
        kotlinSuccess(block = block)
    } catch (e: Exception) {
        if (e is CancellationException) currentCoroutineContext().ensureActive()
        kotlinFailure(throwable = e)
    } finally {
        finally()
    }
}

/**
 * Non-suspend runner that catches and encapsulates [T] and **all thrown [Exceptions][Exception]** as a
 * [KotlinResult][nz.adjmunro.outcome.result.KotlinResult].
 *
 * - ***Will catch [CancellationException][kotlinx.coroutines.CancellationException].** Use
 *   [resultOf][nz.adjmunro.outcome.result.resultOf] in suspend contexts to avoid swallowing cancellation.*
 * - ***Does NOT catch [Error] subtypes.** Errors are always fatal.*
 *
 * ```kotlin
 * val a: KotlinResult<String> = result { throw Exception() } // Result.failure(Exception())
 * val b: KotlinResult<String> = result { "Hello, World!" }   // Result.success("Hello, World!")
 * ```
 *
 * @param T The return type of [block].
 * @param finally An optional cleanup block, always runs after [block].
 * @param block The protected block to execute. May throw an exception.
 * @return The [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] of [block].
 * @throws Error Always propagated — errors are fatal.
 * @see nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
 * @see nz.adjmunro.outcome.result.resultOf
 */
@UnsafeForCoroutineCancellation
public inline fun <T> result(
    finally: () -> Unit = ::unit,
    block: () -> T,
): KotlinResult<T> {
    contract {
        callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE)
        callsInPlace(lambda = finally, kind = InvocationKind.EXACTLY_ONCE)
    }

    return try {
        kotlinSuccess(block = block)
    } catch (e: Exception) {
        kotlinFailure(throwable = e)
    } finally {
        finally()
    }
}
