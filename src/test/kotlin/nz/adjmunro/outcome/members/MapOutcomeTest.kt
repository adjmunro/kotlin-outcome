package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class MapOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 4)
    private val failure: Outcome<Int, String> = Failure(error = "err")

    // ── map ───────────────────────────────────────────────────────────────────

    @Test
    fun `map transforms both sides`() {
        success.map(failure = { it + "!" }) { it * 10 }.shouldBe(expected = Success(value = 40))
        failure.map(failure = { it + "!" }) { it * 10 }.shouldBe(expected = Failure(error = "err!"))
    }

    // ── mapSuccess ────────────────────────────────────────────────────────────

    @Test
    fun `mapSuccess transforms value`() {
        success.mapSuccess { it * 2 }.shouldBe(expected = Success(value = 8))
    }

    @Test
    fun `mapSuccess does not affect Failure`() {
        failure.mapSuccess { it * 2 }.shouldBe(expected = failure)
    }

    // ── mapFailure ────────────────────────────────────────────────────────────

    @Test
    fun `mapFailure transforms error`() {
        failure.mapFailure { it.length }.shouldBe(expected = Failure(error = 3))
    }

    @Test
    fun `mapFailure does not affect Success`() {
        success.mapFailure { it.length }.shouldBe(expected = success)
    }

    // ── invert ────────────────────────────────────────────────────────────────

    @Test
    fun `invert turns Success into Failure`() {
        success.invert().shouldBe(expected = Failure(error = 4))
    }

    @Test
    fun `invert turns Failure into Success`() {
        failure.invert().shouldBe(expected = Success(value = "err"))
    }

    @Test
    fun `invert twice returns original`() {
        success.invert().invert().shouldBe(expected = success)
    }
}
