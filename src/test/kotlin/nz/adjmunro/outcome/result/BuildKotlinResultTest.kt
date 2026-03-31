package nz.adjmunro.outcome.result

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import org.junit.jupiter.api.Test

class BuildKotlinResultTest {

    // ── kotlinSuccess ─────────────────────────────────────────────────────────

    @Test
    fun `kotlinSuccess wraps value`() {
        kotlinSuccess(value = 42).getOrThrow().shouldBe(expected = 42)
    }

    @Test
    fun `kotlinSuccess block wraps result`() {
        kotlinSuccess { "hello" }.getOrThrow().shouldBe(expected = "hello")
    }

    @Test
    fun `kotlinSuccess extension wraps result`() {
        val result: KotlinResult<Int> = 5.kotlinSuccess { this * 2 }
        result.getOrThrow().shouldBe(expected = 10)
    }

    // ── kotlinFailure ─────────────────────────────────────────────────────────

    @Test
    fun `kotlinFailure wraps throwable`() {
        val ex: RuntimeException = RuntimeException("boom")
        kotlinFailure(throwable = ex).exceptionOrThrow().shouldBe(expected = ex)
    }

    @Test
    fun `kotlinFailure block wraps result`() {
        val ex: IllegalStateException = IllegalStateException("block")
        kotlinFailure { ex }.exceptionOrThrow().shouldBe(expected = ex)
    }

    // ── resultOf (suspend, cancellation-safe) ─────────────────────────────────

    @Test
    fun `resultOf wraps success as Result success`(): TestResult = runTest {
        resultOf { 42 }.getOrThrow().shouldBe(expected = 42)
    }

    @Test
    fun `resultOf wraps exception as Result failure`(): TestResult = runTest {
        val ex: RuntimeException = RuntimeException("err")
        resultOf<Int> { throw ex }.exceptionOrThrow().shouldBe(expected = ex)
    }

    @Test
    fun `resultOf wraps CancellationException as failure when context is active`(): TestResult = runTest {
        val ex: CancellationException = CancellationException("cancel")
        resultOf<Int> { throw ex }.exceptionOrThrow().shouldBe(expected = ex)
    }

    @Test
    fun `resultOf runs finally block on success`(): TestResult = runTest {
        var finalized: Boolean = false
        resultOf(finally = { finalized = true }) { 1 }
        finalized.shouldBe(expected = true)
    }

    @Test
    fun `resultOf runs finally block on failure`(): TestResult = runTest {
        var finalized: Boolean = false
        resultOf<Int>(finally = { finalized = true }) { throw RuntimeException() }
        finalized.shouldBe(expected = true)
    }

    // ── result (non-suspend) ──────────────────────────────────────────────────

    @Test
    fun `result wraps success`() {
        @OptIn(UnsafeForCoroutineCancellation::class)
        result { "ok" }.getOrThrow().shouldBe(expected = "ok")
    }

    @Test
    fun `result wraps exception`() {
        val ex: RuntimeException = RuntimeException("e")
        @OptIn(UnsafeForCoroutineCancellation::class)
        result<Int> { throw ex }.exceptionOrThrow().shouldBe(expected = ex)
    }

    @Test
    fun `result swallows CancellationException`() {
        @OptIn(UnsafeForCoroutineCancellation::class)
        val r: KotlinResult<Int> = result<Int> { throw CancellationException("oops") }
        r.isFailure.shouldBe(expected = true)
    }
}
