package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class ToOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 1)
    private val failure: Outcome<Int, String> = Failure(error = "e")

    // ── asFault ───────────────────────────────────────────────────────────────

    @Test
    fun `asFault maps Success value to Unit`() {
        success.asFault.shouldBe(expected = Success(value = Unit))
    }

    @Test
    fun `asFault preserves Failure error`() {
        failure.asFault.shouldBe(expected = Failure(error = "e"))
    }

    // ── asMaybe ───────────────────────────────────────────────────────────────

    @Test
    fun `asMaybe maps Failure error to Unit`() {
        failure.asMaybe.shouldBe(expected = Failure(error = Unit))
    }

    @Test
    fun `asMaybe preserves Success value`() {
        success.asMaybe.shouldBe(expected = Success(value = 1))
    }

    // ── asSuccess / asFailure ─────────────────────────────────────────────────

    @Test
    fun `asSuccess wraps directly as Success`() {
        42.asSuccess.shouldBe(expected = Success(value = 42))
    }

    @Test
    fun `asFailure wraps directly as Failure`() {
        "error".asFailure.shouldBe(expected = Failure(error = "error"))
    }

    @Test
    fun `asSuccess does not smart-wrap Throwable`() {
        val ex: RuntimeException = RuntimeException("boom")
        ex.asSuccess.shouldBe(expected = Success(value = ex))
    }

    // ── toOutcome property ────────────────────────────────────────────────────

    @Test
    fun `toOutcome wraps normal value as Success`() {
        42.toOutcome.shouldBe(expected = Success(value = 42))
    }

    @Test
    fun `toOutcome wraps Throwable as Failure`() {
        val ex: RuntimeException = RuntimeException("boom")
        ex.toOutcome.shouldBe(expected = Failure(error = ex))
    }

    @Test
    fun `toOutcome wraps null as NullPointerException Failure`() {
        val o: Outcome<Any, Throwable> = null.toOutcome
        o.isFailure().shouldBe(expected = true)
    }

    // ── toMaybe property ──────────────────────────────────────────────────────

    @Test
    fun `toMaybe wraps normal value as Success`() {
        "hello".toMaybe.shouldBe(expected = Success(value = "hello"))
    }

    @Test
    fun `toMaybe wraps Throwable as emptyFailure`() {
        RuntimeException().toMaybe.shouldBe(expected = emptyFailure())
    }

    @Test
    fun `toMaybe wraps null as emptyFailure`() {
        null.toMaybe.shouldBe(expected = emptyFailure())
    }

    // ── toFault property ──────────────────────────────────────────────────────

    @Test
    fun `toFault wraps Throwable as Failure`() {
        val ex: RuntimeException = RuntimeException("x")
        (ex.toFault as Failure).error.shouldBe(expected = ex)
    }

    @Test
    fun `toFault wraps non-throwable as emptySuccess`() {
        "ok".toFault.shouldBe(expected = emptySuccess())
    }

    @Test
    fun `toFault wraps null as NullPointerException Failure`() {
        null.toFault.isFailure().shouldBe(expected = true)
    }

    // ── toOutcome(predicate) ──────────────────────────────────────────────────

    @Test
    fun `toOutcome predicate returns Success when true`() {
        5.toOutcome(predicate = { this > 0 }) { "neg" }.shouldBe(expected = Success(value = 5))
    }

    @Test
    fun `toOutcome predicate returns Failure when false`() {
        (-1).toOutcome(predicate = { this > 0 }) { "negative" }.shouldBe(expected = Failure(error = "negative"))
    }

    // ── toMaybe(isSuccess) ────────────────────────────────────────────────────

    @Test
    fun `toMaybe predicate returns Success when true`() {
        "hi".toMaybe { length > 0 }.shouldBe(expected = Success(value = "hi"))
    }

    @Test
    fun `toMaybe predicate returns emptyFailure when false`() {
        "".toMaybe { length > 0 }.shouldBe(expected = emptyFailure())
    }

    // ── toFault(isFailure) ────────────────────────────────────────────────────

    @Test
    fun `toFault predicate returns Failure when true`() {
        (-1).toFault { this < 0 }.shouldBe(expected = Failure(error = -1))
    }

    @Test
    fun `toFault predicate returns emptySuccess when false`() {
        1.toFault { this < 0 }.shouldBe(expected = emptySuccess())
    }
}
