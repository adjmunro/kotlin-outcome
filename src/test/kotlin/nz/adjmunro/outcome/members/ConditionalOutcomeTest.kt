package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class ConditionalOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 1)
    private val failure: Outcome<Int, String> = Failure(error = "err")

    // ── isSuccess ─────────────────────────────────────────────────────────────

    @Test
    fun `isSuccess returns true for Success`() {
        success.isSuccess().shouldBe(expected = true)
    }

    @Test
    fun `isSuccess returns false for Failure`() {
        failure.isSuccess().shouldBe(expected = false)
    }

    @Test
    fun `isSuccess with matching predicate returns true`() {
        success.isSuccess { it > 0 }.shouldBe(expected = true)
    }

    @Test
    fun `isSuccess with failing predicate returns false`() {
        success.isSuccess { it < 0 }.shouldBe(expected = false)
    }

    @Test
    fun `isSuccess with predicate on Failure returns false`() {
        failure.isSuccess { true }.shouldBe(expected = false)
    }

    // ── isFailure ─────────────────────────────────────────────────────────────

    @Test
    fun `isFailure returns true for Failure`() {
        failure.isFailure().shouldBe(expected = true)
    }

    @Test
    fun `isFailure returns false for Success`() {
        success.isFailure().shouldBe(expected = false)
    }

    @Test
    fun `isFailure with matching predicate returns true`() {
        failure.isFailure { it == "err" }.shouldBe(expected = true)
    }

    @Test
    fun `isFailure with failing predicate returns false`() {
        failure.isFailure { it == "other" }.shouldBe(expected = false)
    }

    @Test
    fun `isFailure with predicate on Success returns false`() {
        success.isFailure { true }.shouldBe(expected = false)
    }

    // ── onSuccess ─────────────────────────────────────────────────────────────

    @Test
    fun `onSuccess invokes block for Success`() {
        var called: Boolean = false
        success.onSuccess { called = true }
        called.shouldBe(expected = true)
    }

    @Test
    fun `onSuccess does not invoke block for Failure`() {
        var called: Boolean = false
        failure.onSuccess { called = true }
        called.shouldBe(expected = false)
    }

    @Test
    fun `onSuccess returns the original Outcome`() {
        val result: Outcome<Int, String> = success.onSuccess { }
        result.shouldBe(expected = success)
    }

    @Test
    fun `onSuccess returns original Failure unchanged`() {
        val result: Outcome<Int, String> = failure.onSuccess { }
        result.shouldBe(expected = failure)
    }

    // ── onFailure ─────────────────────────────────────────────────────────────

    @Test
    fun `onFailure invokes block for Failure`() {
        var called: Boolean = false
        failure.onFailure { called = true }
        called.shouldBe(expected = true)
    }

    @Test
    fun `onFailure does not invoke block for Success`() {
        var called: Boolean = false
        success.onFailure { called = true }
        called.shouldBe(expected = false)
    }

    @Test
    fun `onFailure returns the original Outcome`() {
        val result: Outcome<Int, String> = failure.onFailure { }
        result.shouldBe(expected = failure)
    }

    @Test
    fun `onSuccess and onFailure can chain`() {
        var successCalled: Boolean = false
        var failureCalled: Boolean = false
        success
            .onSuccess { successCalled = true }
            .onFailure { failureCalled = true }
        successCalled.shouldBe(expected = true)
        failureCalled.shouldBe(expected = false)
    }
}
