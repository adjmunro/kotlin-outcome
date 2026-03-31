package nz.adjmunro.outcome.members

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raise
import org.junit.jupiter.api.Test

class GetOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 42)
    private val failure: Outcome<Int, String> = Failure(error = "err")

    // ── getOrDefault ──────────────────────────────────────────────────────────

    @Test
    fun `getOrDefault returns value for Success`() {
        success.getOrDefault(default = 0).shouldBe(expected = 42)
    }

    @Test
    fun `getOrDefault returns default for Failure`() {
        failure.getOrDefault(default = 99).shouldBe(expected = 99)
    }

    // ── getOrElse ─────────────────────────────────────────────────────────────

    @Test
    fun `getOrElse returns value for Success`() {
        success.getOrElse { -1 }.shouldBe(expected = 42)
    }

    @Test
    fun `getOrElse invokes recover for Failure`() {
        failure.getOrElse { it.length }.shouldBe(expected = 3)
    }

    // ── getOrNull ─────────────────────────────────────────────────────────────

    @Test
    fun `getOrNull returns value for Success`() {
        success.getOrNull().shouldBe(expected = 42)
    }

    @Test
    fun `getOrNull returns null for Failure`() {
        failure.getOrNull().shouldBe(expected = null)
    }

    @Test
    fun `getOrNull with nullable value type returns value`() {
        val o: Outcome<Int?, String> = Success(value = null)
        o.getOrNull().shouldBe(expected = null) // ambiguous but correct; null value in Success
    }

    // ── getOrThrow ────────────────────────────────────────────────────────────

    @Test
    fun `getOrThrow returns value for Success`() {
        success.getOrThrow().shouldBe(expected = 42)
    }

    @Test
    fun `getOrThrow throws for Failure`() {
        shouldThrow<Throwable> { failure.getOrThrow() }
    }

    // ── getOrRaise ────────────────────────────────────────────────────────────

    @Test
    fun `getOrRaise returns value in active RaiseScope`(): TestResult = runTest {
        val inner: Outcome<Int, String> = Success(value = 7)
        val result: Outcome<Int, String> = outcomeOf { inner.getOrRaise() }
        result.shouldBe(expected = Success(value = 7))
    }

    @Test
    fun `getOrRaise short-circuits on Failure`(): TestResult = runTest {
        val inner: Outcome<Int, String> = Failure(error = "propagated")
        val result: Outcome<Int, String> = outcomeOf { inner.getOrRaise() }
        result.shouldBe(expected = Failure(error = "propagated"))
    }

    // ── errorOrDefault ────────────────────────────────────────────────────────

    @Test
    fun `errorOrDefault returns error for Failure`() {
        failure.errorOrDefault(default = "default").shouldBe(expected = "err")
    }

    @Test
    fun `errorOrDefault returns default for Success`() {
        success.errorOrDefault(default = "default").shouldBe(expected = "default")
    }

    // ── errorOrElse ───────────────────────────────────────────────────────────

    @Test
    fun `errorOrElse returns error for Failure`() {
        failure.errorOrElse { it.toString() }.shouldBe(expected = "err")
    }

    @Test
    fun `errorOrElse invokes faulter for Success`() {
        success.errorOrElse { "from:$it" }.shouldBe(expected = "from:42")
    }

    // ── errorOrNull ───────────────────────────────────────────────────────────

    @Test
    fun `errorOrNull returns error for Failure`() {
        failure.errorOrNull().shouldBe(expected = "err")
    }

    @Test
    fun `errorOrNull returns null for Success`() {
        success.errorOrNull().shouldBe(expected = null)
    }

    // ── errorOrThrow ──────────────────────────────────────────────────────────

    @Test
    fun `errorOrThrow returns error for Failure`() {
        failure.errorOrThrow().shouldBe(expected = "err")
    }

    @Test
    fun `errorOrThrow throws for Success`() {
        shouldThrow<Throwable> { success.errorOrThrow() }
    }

    // ── errorOrRaise ──────────────────────────────────────────────────────────

    @Test
    fun `errorOrRaise returns error in RaiseScope when Failure`(): TestResult = runTest {
        // errorOrRaise: context(scope: RaiseScope<OuterError>) where Ok: OuterError
        // inner: Outcome<String, String> — both Ok and Error are String so Ok: OuterError=String
        val inner: Outcome<String, String> = Failure(error = "e")
        var captured: String? = null
        val result: Outcome<Nothing, String> = outcomeOf {
            captured = inner.errorOrRaise()
            raise { captured ?: "unreachable" }
        }
        result.shouldBe(expected = Failure(error = "e"))
        captured.shouldBe(expected = "e")
    }

    @Test
    fun `errorOrRaise short-circuits on Success`(): TestResult = runTest {
        // errorOrRaise: Ok: OuterError, so with inner: Outcome<String, String>, OuterError=String
        // On Success("ok"), errorOrRaise raises "ok" into the outer scope → Failure("ok")
        val inner: Outcome<String, String> = Success(value = "ok")
        val result: Outcome<Nothing, String> = outcomeOf {
            inner.errorOrRaise()
            raise { "unreachable" }
        }
        result.shouldBe(expected = Failure(error = "ok"))
    }
}
