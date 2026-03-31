package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class FlatMapOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 4)
    private val failure: Outcome<Int, String> = Failure(error = "err")

    // ── flatMapSuccess ────────────────────────────────────────────────────────

    @Test
    fun `flatMapSuccess transforms Success to new Outcome`() {
        val result: Outcome<Int, String> = success.flatMapSuccess { Success(value = it * 10) }
        result.shouldBe(expected = Success(value = 40))
    }

    @Test
    fun `flatMapSuccess can produce Failure from Success`() {
        val result: Outcome<Int, String> = success.flatMapSuccess { Failure(error = "converted") }
        result.shouldBe(expected = Failure(error = "converted"))
    }

    @Test
    fun `flatMapSuccess does not affect Failure`() {
        failure.flatMapSuccess { Success(value = 99) }.shouldBe(expected = failure)
    }

    // ── flatMapFailure ────────────────────────────────────────────────────────

    @Test
    fun `flatMapFailure transforms Failure to new Outcome`() {
        val result: Outcome<Int, String> = failure.flatMapFailure { Success(value = it.length) }
        result.shouldBe(expected = Success(value = 3))
    }

    @Test
    fun `flatMapFailure can produce new Failure from Failure`() {
        val result: Outcome<Int, String> = failure.flatMapFailure { Failure(error = "new: $it") }
        result.shouldBe(expected = Failure(error = "new: err"))
    }

    @Test
    fun `flatMapFailure does not affect Success`() {
        success.flatMapFailure { Failure(error = "nope") }.shouldBe(expected = success)
    }

    // ── contrast with map ─────────────────────────────────────────────────────

    @Test
    fun `flatMapSuccess differs from mapSuccess in that transform returns Outcome`() {
        // mapSuccess { it * 2 } wraps result automatically → Success(8)
        // flatMapSuccess { Success(it * 2) } must wrap manually, same result
        val mapResult: Outcome<Int, String> = success.mapSuccess { it * 2 }
        val flatResult: Outcome<Int, String> = success.flatMapSuccess { Success(value = it * 2) }
        mapResult.shouldBe(expected = flatResult)
    }
}
