package nz.adjmunro.outcome.result

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import org.junit.jupiter.api.Test

@OptIn(UnsafeForCoroutineCancellation::class)
class RescopeKotlinResultTest {

    private val success: KotlinResult<Int> = kotlinSuccess(value = 4)
    private val failure: KotlinResult<Int> = kotlinFailure(throwable = RuntimeException("err"))

    // ── andThenOf (suspend) ───────────────────────────────────────────────────

    @Test
    fun `andThenOf transforms success`(): TestResult = runTest {
        success.andThenOf { it * 2 }.getOrThrow().shouldBe(expected = 8)
    }

    @Test
    fun `andThenOf leaves failure unchanged`(): TestResult = runTest {
        failure.andThenOf { 99 }.isFailure.shouldBe(expected = true)
    }

    @Test
    fun `andThenOf re-encapsulates exception from transform`(): TestResult = runTest {
        val result: KotlinResult<Int> = success.andThenOf { throw RuntimeException("inner") }
        result.isFailure.shouldBe(expected = true)
    }

    // ── andThen (non-suspend) ─────────────────────────────────────────────────

    @Test
    fun `andThen transforms success`() {
        success.andThen { it * 3 }.getOrThrow().shouldBe(expected = 12)
    }

    @Test
    fun `andThen leaves failure unchanged`() {
        failure.andThen { 99 }.isFailure.shouldBe(expected = true)
    }

    @Test
    fun `andThen re-encapsulates exception unlike flatMap`() {
        // flatMap would propagate; andThen re-encapsulates
        val result: KotlinResult<Int> = success.andThen { throw RuntimeException("wrapped") }
        result.isFailure.shouldBe(expected = true)
    }

    // ── andIf (non-suspend) ───────────────────────────────────────────────────

    @Test
    fun `andIf transforms when predicate true`() {
        success.andIf(predicate = { it > 0 }) { it + 100 }
            .getOrThrow().shouldBe(expected = 104)
    }

    @Test
    fun `andIf leaves value unchanged when predicate false`() {
        success.andIf(predicate = { it < 0 }) { it + 100 }
            .getOrThrow().shouldBe(expected = 4)
    }

    @Test
    fun `andIf leaves failure unchanged`() {
        failure.andIf(predicate = { true }) { it }.isFailure.shouldBe(expected = true)
    }

    // ── andThenOf predicate (suspend) ─────────────────────────────────────────

    @Test
    fun `andThenOf predicate transforms when predicate true`(): TestResult = runTest {
        success.andThenOf(predicate = { it > 0 }) { it + 10 }.getOrThrow().shouldBe(expected = 14)
    }

    @Test
    fun `andThenOf predicate leaves value when predicate false`(): TestResult = runTest {
        success.andThenOf(predicate = { it < 0 }) { it + 10 }.getOrThrow().shouldBe(expected = 4)
    }

    // ── tryRecoverOf (suspend) ────────────────────────────────────────────────

    @Test
    fun `tryRecoverOf converts failure to success`(): TestResult = runTest {
        failure.tryRecoverOf { 99 }.getOrThrow().shouldBe(expected = 99)
    }

    @Test
    fun `tryRecoverOf leaves success unchanged`(): TestResult = runTest {
        success.tryRecoverOf { -1 }.getOrThrow().shouldBe(expected = 4)
    }

    @Test
    fun `tryRecoverOf re-encapsulates exception from transform`(): TestResult = runTest {
        val result: KotlinResult<Int> = failure.tryRecoverOf { throw RuntimeException("inner") }
        result.isFailure.shouldBe(expected = true)
    }

    // ── tryRecover (non-suspend) ──────────────────────────────────────────────

    @Test
    fun `tryRecover converts failure to success`() {
        failure.tryRecover { 42 }.getOrThrow().shouldBe(expected = 42)
    }

    @Test
    fun `tryRecover leaves success unchanged`() {
        success.tryRecover { -1 }.getOrThrow().shouldBe(expected = 4)
    }
}
