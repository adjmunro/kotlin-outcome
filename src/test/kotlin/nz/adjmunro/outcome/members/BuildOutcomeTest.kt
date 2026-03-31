package nz.adjmunro.outcome.members

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raise
import org.junit.jupiter.api.Test

@OptIn(UnsafeForCoroutineCancellation::class)
class BuildOutcomeTest {

    // ── emptyFailure / emptySuccess ───────────────────────────────────────────

    @Test
    fun `emptyFailure produces Failure of Unit`() {
        emptyFailure().shouldBe(expected = Failure(error = Unit))
    }

    @Test
    fun `emptyFailure ignores argument`() {
        emptyFailure(ignore = "ignored").shouldBe(expected = Failure(error = Unit))
    }

    @Test
    fun `emptySuccess produces Success of Unit`() {
        emptySuccess().shouldBe(expected = Success(value = Unit))
    }

    @Test
    fun `emptySuccess ignores argument`() {
        emptySuccess(ignore = 42).shouldBe(expected = Success(value = Unit))
    }

    // ── failure / success builders ────────────────────────────────────────────

    @Test
    fun `failure block produces Failure`() {
        failure { "error" }.shouldBe(expected = Failure(error = "error"))
    }

    @Test
    fun `success block produces Success`() {
        success { 99 }.shouldBe(expected = Success(value = 99))
    }

    // ── outcome ───────────────────────────────────────────────────────────────

    @Test
    fun `outcome wraps block result as Success`() {
        val result: Outcome<Int, String> = outcome<Int, String> { 42 }
        result.shouldBe(expected = Success(value = 42))
    }

    @Test
    fun `outcome with raise produces Failure`() {
        val result: Outcome<Int, String> = outcome { raise { "oops" } }
        result.shouldBe(expected = Failure(error = "oops"))
    }

    @Test
    fun `outcome default catch rethrows exception`() {
        shouldThrow<RuntimeException> {
            outcome<Int, String> { throw RuntimeException("boom") }
        }
    }

    @Test
    fun `outcome custom catch converts exception to Failure`() {
        val result: Outcome<Int, Exception> = outcome<Int, Exception>(catch = ::Failure) {
            throw IllegalStateException("bad")
        }
        result.isFailure().shouldBe(expected = true)
        (result as Failure).error.message.shouldBe(expected = "bad")
    }

    // ── fault ─────────────────────────────────────────────────────────────────

    @Test
    fun `fault wraps Unit-returning block as emptySuccess`() {
        val result: Fault<String> = fault { /* nothing raised */ }
        result.shouldBe(expected = emptySuccess())
    }

    @Test
    fun `fault with raise produces Failure`() {
        val result: Fault<String> = fault { raise { "fail" } }
        result.shouldBe(expected = Failure(error = "fail"))
    }

    @Test
    fun `fault default catch rethrows`() {
        shouldThrow<RuntimeException> {
            fault<String> { throw RuntimeException() }
        }
    }

    // ── maybe ─────────────────────────────────────────────────────────────────

    @Test
    fun `maybe wraps value as Success`() {
        val result: Maybe<Int> = maybe { 7 }
        result.shouldBe(expected = Success(value = 7))
    }

    @Test
    fun `maybe with raise produces emptyFailure`() {
        val result: Maybe<Int> = maybe { raise { Unit } }
        result.shouldBe(expected = emptyFailure())
    }

    @Test
    fun `maybe default catch returns emptyFailure on exception`() {
        val result: Maybe<Int> = maybe { throw RuntimeException() }
        result.shouldBe(expected = emptyFailure())
    }

    // ── catchException ────────────────────────────────────────────────────────

    @Test
    fun `catchException wraps success`() {
        val result: Outcome<Int, Exception> = catchException { 5 }
        result.shouldBe(expected = Success(value = 5))
    }

    @Test
    fun `catchException wraps thrown exception as Failure`() {
        val ex: IllegalArgumentException = IllegalArgumentException("bad input")
        val result: Outcome<Int, Exception> = catchException { throw ex }
        result.shouldBe(expected = Failure(error = ex))
    }

    // ── catchString ───────────────────────────────────────────────────────────

    @Test
    fun `catchString wraps success`() {
        val result: Outcome<Int, String> = catchString { 10 }
        result.shouldBe(expected = Success(value = 10))
    }

    @Test
    fun `catchString converts exception message to Failure`() {
        val result: Outcome<Int, String> = catchString { throw RuntimeException("oops") }
        result.shouldBe(expected = Failure(error = "oops"))
    }

    @Test
    fun `catchString uses toString when message is null`() {
        val ex: RuntimeException = object : RuntimeException() {
            override val message: String? = null
            override fun toString(): String = "custom-string"
        }
        val result: Outcome<Int, String> = catchString { throw ex }
        (result as Failure).error.shouldBe(expected = "custom-string")
    }
}
