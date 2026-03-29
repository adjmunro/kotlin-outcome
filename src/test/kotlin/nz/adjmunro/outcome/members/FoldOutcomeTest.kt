package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class FoldOutcomeTest {

    private val success: Outcome<Int, String> = Success(value = 42)
    private val failure: Outcome<Int, String> = Failure(error = "err")

    // ── fold ──────────────────────────────────────────────────────────────────

    @Test
    fun `fold applies success lambda for Success`() {
        success.fold(failure = { -1 }) { value * 2 }.shouldBe(expected = 84)
    }

    @Test
    fun `fold applies failure lambda for Failure`() {
        failure.fold(failure = { error.length }) { -1 }.shouldBe(expected = 3)
    }

    @Test
    fun `fold can return any Output type`() {
        val result: String = success.fold(failure = { "err:$error" }) { "ok:$value" }
        result.shouldBe(expected = "ok:42")
    }

    @Test
    fun `fold failure returns string`() {
        val result: String = failure.fold(failure = { "err:$error" }) { "ok" }
        result.shouldBe(expected = "err:err")
    }

    // ── rfold ─────────────────────────────────────────────────────────────────

    @Test
    fun `rfold applies success lambda for Success`() {
        // rfold is fold with swapped argument order
        success.rfold(success = { value + 1 }) { -1 }.shouldBe(expected = 43)
    }

    @Test
    fun `rfold applies failure lambda for Failure`() {
        failure.rfold(success = { -1 }) { error.uppercase() }.shouldBe(expected = "ERR")
    }

    @Test
    fun `rfold and fold produce same result`() {
        val foldResult: Int = success.fold(failure = { 0 }) { value }
        val rfoldResult: Int = success.rfold(success = { value }) { 0 }
        foldResult.shouldBe(expected = rfoldResult)
    }

    // ── collapse ──────────────────────────────────────────────────────────────

    @Test
    fun `collapse returns value for Success`() {
        val o: Outcome<Int, Int> = Success(value = 5)
        o.collapse().shouldBe(expected = 5)
    }

    @Test
    fun `collapse returns error for Failure`() {
        val o: Outcome<Int, Int> = Failure(error = 9)
        o.collapse().shouldBe(expected = 9)
    }

    @Test
    fun `collapse with common ancestor type`() {
        val o: Outcome<String, CharSequence> = Success(value = "hello")
        val result: CharSequence = o.collapse()
        result.shouldBe(expected = "hello")
    }

    @Test
    fun `collapse with Failure and ancestor`() {
        val o: Outcome<String, CharSequence> = Failure(error = StringBuilder("world"))
        val result: CharSequence = o.collapse()
        result.toString().shouldBe(expected = "world")
    }
}
