package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.throwable.ThrowableWrapper
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows

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

    // ── throwIf ───────────────────────────────────────────────────────────────

    @Test
    fun `throwIf does not throw for Success`() {
        success.throwIf { true }
    }

    @Test
    fun `throwIf returns original Success unchanged`() {
        val result: Outcome<Int, String> = success.throwIf { true }
        result.shouldBe(expected = success)
    }

    @Test
    fun `throwIf does not throw when predicate returns false`() {
        failure.throwIf { false }
    }

    @Test
    fun `throwIf returns original Failure when predicate returns false`() {
        val result: Outcome<Int, String> = failure.throwIf { false }
        result.shouldBe(expected = failure)
    }

    @Test
    fun `throwIf predicate is not invoked for Success`() {
        var called: Boolean = false
        success.throwIf { called = true; true }
        called.shouldBe(expected = false)
    }

    @Test
    fun `throwIf throws IllegalStateException for non-throwable error`() {
        assertThrows<IllegalStateException> {
            failure.throwIf { true }
        }
    }

    @Test
    fun `throwIf uses custom fallbackMessage for non-throwable error`() {
        val ex: IllegalStateException = assertThrows<IllegalStateException> {
            failure.throwIf(fallbackMessage = { "custom: $it" }) { true }
        }
        ex.message.shouldBe(expected = "custom: err")
    }

    @Test
    fun `throwIf default fallbackMessage contains error value`() {
        val ex: IllegalStateException = assertThrows<IllegalStateException> {
            failure.throwIf { true }
        }
        (ex.message?.contains("err")).shouldBe(expected = true)
    }

    @Test
    fun `throwIf throws the Throwable error directly`() {
        val error: RuntimeException = RuntimeException("boom")
        val outcome: Outcome<Int, RuntimeException> = Failure(error = error)
        val ex: RuntimeException = assertThrows<RuntimeException> {
            outcome.throwIf { true }
        }
        ex.shouldBe(expected = error)
    }

    @Test
    fun `throwIf ignores fallbackMessage when error is Throwable`() {
        val error: RuntimeException = RuntimeException("original")
        val outcome: Outcome<Int, RuntimeException> = Failure(error = error)
        var messageCalled: Boolean = false
        val ex: RuntimeException = assertThrows<RuntimeException> {
            outcome.throwIf(fallbackMessage = { messageCalled = true; "ignored" }) { true }
        }
        messageCalled.shouldBe(expected = false)
        ex.message.shouldBe(expected = "original")
    }

    @Test
    fun `throwIf throws ThrowableWrapper cause directly`() {
        val cause: IllegalArgumentException = IllegalArgumentException("wrapped")
        val wrapper: ThrowableWrapper<IllegalArgumentException> = object : ThrowableWrapper<IllegalArgumentException> {
            override val cause: IllegalArgumentException = cause
        }
        val outcome: Outcome<Int, ThrowableWrapper<IllegalArgumentException>> = Failure(error = wrapper)
        val ex: IllegalArgumentException = assertThrows<IllegalArgumentException> {
            outcome.throwIf { true }
        }
        ex.shouldBe(expected = cause)
    }

    @Test
    fun `throwIf throws NullPointerException when error is null`() {
        val outcome: Outcome<Int, String?> = Failure(error = null)
        assertThrows<NullPointerException> {
            outcome.throwIf { true }
        }
    }

    @Test
    fun `throwIf NullPointerException uses fallbackMessage when error is null`() {
        val outcome: Outcome<Int, String?> = Failure(error = null)
        val ex: NullPointerException = assertThrows<NullPointerException> {
            outcome.throwIf(fallbackMessage = { "null error occurred" }) { true }
        }
        ex.message.shouldBe(expected = "null error occurred")
    }

    // ── throwUnless ───────────────────────────────────────────────────────────

    @Test
    fun `throwUnless does not throw for Success`() {
        success.throwUnless { false }
    }

    @Test
    fun `throwUnless returns original Success unchanged`() {
        val result: Outcome<Int, String> = success.throwUnless { false }
        result.shouldBe(expected = success)
    }

    @Test
    fun `throwUnless does not throw when predicate returns true`() {
        failure.throwUnless { true }
    }

    @Test
    fun `throwUnless returns original Failure when predicate returns true`() {
        val result: Outcome<Int, String> = failure.throwUnless { true }
        result.shouldBe(expected = failure)
    }

    @Test
    fun `throwUnless predicate is not invoked for Success`() {
        var called: Boolean = false
        success.throwUnless { called = true; false }
        called.shouldBe(expected = false)
    }

    @Test
    fun `throwUnless throws IllegalStateException for non-throwable error`() {
        assertThrows<IllegalStateException> {
            failure.throwUnless { false }
        }
    }

    @Test
    fun `throwUnless uses custom fallbackMessage for non-throwable error`() {
        val ex: IllegalStateException = assertThrows<IllegalStateException> {
            failure.throwUnless(fallbackMessage = { "custom: $it" }) { false }
        }
        ex.message.shouldBe(expected = "custom: err")
    }

    @Test
    fun `throwUnless default fallbackMessage contains error value`() {
        val ex: IllegalStateException = assertThrows<IllegalStateException> {
            failure.throwUnless { false }
        }
        (ex.message?.contains("err")).shouldBe(expected = true)
    }

    @Test
    fun `throwUnless throws the Throwable error directly`() {
        val error: RuntimeException = RuntimeException("boom")
        val outcome: Outcome<Int, RuntimeException> = Failure(error = error)
        val ex: RuntimeException = assertThrows<RuntimeException> {
            outcome.throwUnless { false }
        }
        ex.shouldBe(expected = error)
    }

    @Test
    fun `throwUnless ignores fallbackMessage when error is Throwable`() {
        val error: RuntimeException = RuntimeException("original")
        val outcome: Outcome<Int, RuntimeException> = Failure(error = error)
        var messageCalled: Boolean = false
        val ex: RuntimeException = assertThrows<RuntimeException> {
            outcome.throwUnless(fallbackMessage = { messageCalled = true; "ignored" }) { false }
        }
        messageCalled.shouldBe(expected = false)
        ex.message.shouldBe(expected = "original")
    }

    @Test
    fun `throwUnless throws ThrowableWrapper cause directly`() {
        val cause: IllegalArgumentException = IllegalArgumentException("wrapped")
        val wrapper: ThrowableWrapper<IllegalArgumentException> = object : ThrowableWrapper<IllegalArgumentException> {
            override val cause: IllegalArgumentException = cause
        }
        val outcome: Outcome<Int, ThrowableWrapper<IllegalArgumentException>> = Failure(error = wrapper)
        val ex: IllegalArgumentException = assertThrows<IllegalArgumentException> {
            outcome.throwUnless { false }
        }
        ex.shouldBe(expected = cause)
    }

    @Test
    fun `throwUnless throws NullPointerException when error is null`() {
        val outcome: Outcome<Int, String?> = Failure(error = null)
        assertThrows<NullPointerException> {
            outcome.throwUnless { false }
        }
    }

    @Test
    fun `throwUnless NullPointerException uses fallbackMessage when error is null`() {
        val outcome: Outcome<Int, String?> = Failure(error = null)
        val ex: NullPointerException = assertThrows<NullPointerException> {
            outcome.throwUnless(fallbackMessage = { "null error occurred" }) { false }
        }
        ex.message.shouldBe(expected = "null error occurred")
    }
}
