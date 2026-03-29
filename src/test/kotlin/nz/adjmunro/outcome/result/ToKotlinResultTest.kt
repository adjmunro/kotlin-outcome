package nz.adjmunro.outcome.result

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import org.junit.jupiter.api.Test

@OptIn(UnsafeForCoroutineCancellation::class)
class ToKotlinResultTest {

    // ── asKotlinResult ────────────────────────────────────────────────────────

    @Test
    fun `asKotlinResult wraps normal value as success`() {
        42.asKotlinResult.getOrThrow().shouldBe(expected = 42)
    }

    @Test
    fun `asKotlinResult wraps Throwable as success (no throw, no catch)`() {
        val ex: RuntimeException = RuntimeException("boom")
        (ex as Any).asKotlinResult.isSuccess.shouldBe(expected = true)
    }

    // ── asKotlinSuccess ───────────────────────────────────────────────────────

    @Test
    fun `asKotlinSuccess wraps value directly as success`() {
        "hi".asKotlinSuccess.getOrThrow().shouldBe(expected = "hi")
    }

    @Test
    fun `asKotlinSuccess wraps Throwable as success without unwrapping`() {
        val ex: RuntimeException = RuntimeException("trick")
        ex.asKotlinSuccess.getOrThrow().shouldBe(expected = ex)
    }

    // ── asKotlinFailure ───────────────────────────────────────────────────────

    @Test
    fun `Throwable asKotlinFailure wraps as failure`() {
        val ex: RuntimeException = RuntimeException("err")
        ex.asKotlinFailure.isFailure.shouldBe(expected = true)
        ex.asKotlinFailure.exceptionOrNull().shouldBe(expected = ex)
    }

    @Test
    fun `Any asKotlinFailure converts to IllegalStateException`() {
        42.asKotlinFailure.isFailure.shouldBe(expected = true)
    }

    // ── toKotlinResult (property) on Outcome ──────────────────────────────────

    @Test
    fun `Success toKotlinResult is Result success`() {
        Success(value = 10).toKotlinResult.getOrThrow().shouldBe(expected = 10)
    }

    @Test
    fun `Failure toKotlinResult is Result failure`() {
        Failure(error = "err").toKotlinResult.isFailure.shouldBe(expected = true)
    }

    // ── toKotlinResult (function) ─────────────────────────────────────────────

    @Test
    fun `toKotlinResult function wraps success when predicate true`() {
        5.toKotlinResult(predicate = { this > 0 }).getOrThrow().shouldBe(expected = 5)
    }

    @Test
    fun `toKotlinResult function wraps failure when predicate false`() {
        (-1).toKotlinResult(predicate = { this > 0 }).isFailure.shouldBe(expected = true)
    }

    @Test
    fun `toKotlinResult default predicate wraps non-Throwable as success`() {
        "ok".toKotlinResult().getOrThrow().shouldBe(expected = "ok")
    }

    // ── awaitKotlinResult ─────────────────────────────────────────────────────

    @Test
    fun `awaitKotlinResult wraps successful deferred as success`(): TestResult = runTest {
        val deferred: CompletableDeferred<Int> = CompletableDeferred(42)
        deferred.awaitKotlinResult().getOrThrow().shouldBe(expected = 42)
    }

    @Test
    fun `awaitKotlinResult wraps failed deferred as failure`(): TestResult = runTest {
        val deferred: CompletableDeferred<Int> = CompletableDeferred()
        deferred.completeExceptionally(RuntimeException("fail"))
        deferred.awaitKotlinResult().isFailure.shouldBe(expected = true)
    }
}
