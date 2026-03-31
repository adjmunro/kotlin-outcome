package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class CoerceOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 5)
    private val failure: Outcome<Int, String> = Failure(error = "err")

    // ── coerceToSuccess / recover ─────────────────────────────────────────────

    @Test
    fun `coerceToSuccess converts Failure error to Success value`() {
        failure.coerceToSuccess { it.length }.shouldBe(expected = Success(value = 3))
    }

    @Test
    fun `coerceToSuccess leaves Success unchanged`() {
        success.coerceToSuccess { 0 }.shouldBe(expected = Success(value = 5))
    }

    @Test
    fun `recover is alias for coerceToSuccess`() {
        val coerced: Success<Int> = failure.coerceToSuccess { it.length }
        val recovered: Success<Int> = failure.recover { it.length }
        coerced.shouldBe(expected = recovered)
    }

    // ── coerceToFailure / falter ──────────────────────────────────────────────

    @Test
    fun `coerceToFailure converts Success value to Failure error`() {
        success.coerceToFailure { it.toString() }.shouldBe(expected = Failure(error = "5"))
    }

    @Test
    fun `coerceToFailure leaves Failure unchanged`() {
        failure.coerceToFailure { it.toString() }.shouldBe(expected = Failure(error = "err"))
    }

    @Test
    fun `falter is alias for coerceToFailure`() {
        val coerced: Failure<String> = success.coerceToFailure { it.toString() }
        val faltered: Failure<String> = success.falter { it.toString() }
        coerced.shouldBe(expected = faltered)
    }
}
