package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import org.junit.jupiter.api.Test

@OptIn(UnsafeForCoroutineCancellation::class)
class RescopeOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 4)
    private val failure: Outcome<Int, String> = Failure(error = "err")

    // ── andThenOf (suspend) ───────────────────────────────────────────────────

    @Test
    fun `andThenOf transforms Success value`(): TestResult = runTest {
        val result: Outcome<Int, String> = success.andThenOf { it * 2 }
        result.shouldBe(expected = Success(value = 8))
    }

    @Test
    fun `andThenOf leaves Failure unchanged`(): TestResult = runTest {
        val result: Outcome<Int, String> = failure.andThenOf { it * 2 }
        result.shouldBe(expected = failure)
    }

    @Test
    fun `andThenOf re-encapsulates exception as Failure with custom catch`(): TestResult = runTest {
        val result: Outcome<Int, String> = success.andThenOf(
            catch = { t -> Failure(error = t.message ?: "error") },
        ) { throw IllegalStateException("inner") }
        result.isFailure().shouldBe(expected = true)
    }

    @Test
    fun `andThenOf predicate overload transforms when predicate true`(): TestResult = runTest {
        val result: Outcome<Int, String> = success.andThenOf(predicate = { it > 0 }) { it + 100 }
        result.shouldBe(expected = Success(value = 104))
    }

    @Test
    fun `andThenOf predicate overload leaves value when predicate false`(): TestResult = runTest {
        val result: Outcome<Int, String> = success.andThenOf(predicate = { it < 0 }) { it + 100 }
        result.shouldBe(expected = Success(value = 4))
    }

    // ── andThen (non-suspend) ─────────────────────────────────────────────────

    @Test
    fun `andThen non-suspend transforms Success`() {
        val result: Outcome<Int, String> = success.andThen { it * 3 }
        result.shouldBe(expected = Success(value = 12))
    }

    @Test
    fun `andThen non-suspend leaves Failure unchanged`() {
        val result: Outcome<Int, String> = failure.andThen { it * 3 }
        result.shouldBe(expected = failure)
    }

    // ── tryRecoverOf (suspend) ────────────────────────────────────────────────

    @Test
    fun `tryRecoverOf converts Failure to Success`(): TestResult = runTest {
        val result: Outcome<Int, String> = failure.tryRecoverOf { 99 }
        result.shouldBe(expected = Success(value = 99))
    }

    @Test
    fun `tryRecoverOf leaves Success unchanged`(): TestResult = runTest {
        val result: Outcome<Int, String> = success.tryRecoverOf { 0 }
        result.shouldBe(expected = Success(value = 4))
    }

    // ── tryRecover (non-suspend) ──────────────────────────────────────────────

    @Test
    fun `tryRecover non-suspend converts Failure to Success`() {
        val result: Outcome<Int, String> = failure.tryRecover { it.length }
        result.shouldBe(expected = Success(value = 3))
    }

    @Test
    fun `tryRecover non-suspend leaves Success unchanged`() {
        val result: Outcome<Int, String> = success.tryRecover { 0 }
        result.shouldBe(expected = Success(value = 4))
    }
}
