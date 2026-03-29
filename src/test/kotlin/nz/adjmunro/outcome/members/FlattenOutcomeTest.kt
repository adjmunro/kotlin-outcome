package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class FlattenOutcomeTest {

    // ── flattenNestedSuccess ──────────────────────────────────────────────────

    @Test
    fun `flattenNestedSuccess unwraps inner Success from outer Success`() {
        val nested: Outcome<Outcome<Int, String>, String> = Success(value = Success(value = 42))
        val result: Outcome<Int, String> = nested.flattenNestedSuccess()
        result.shouldBe(expected = Success(value = 42))
    }

    @Test
    fun `flattenNestedSuccess unwraps inner Failure from outer Success`() {
        val nested: Outcome<Outcome<Int, String>, String> = Success(value = Failure(error = "inner"))
        val result: Outcome<Int, String> = nested.flattenNestedSuccess()
        result.shouldBe(expected = Failure(error = "inner"))
    }

    @Test
    fun `flattenNestedSuccess keeps outer Failure unchanged`() {
        val nested: Outcome<Outcome<Int, String>, String> = Failure(error = "outer")
        val result: Outcome<Int, String> = nested.flattenNestedSuccess()
        result.shouldBe(expected = Failure(error = "outer"))
    }

    // ── flattenNestedFailure ──────────────────────────────────────────────────

    @Test
    fun `flattenNestedFailure unwraps inner Success from outer Failure`() {
        val nested: Outcome<Int, Outcome<Int, String>> = Failure(error = Success(value = 7))
        val result: Outcome<Int, String> = nested.flattenNestedFailure()
        result.shouldBe(expected = Success(value = 7))
    }

    @Test
    fun `flattenNestedFailure unwraps inner Failure from outer Failure`() {
        val nested: Outcome<Int, Outcome<Int, String>> = Failure(error = Failure(error = "deep"))
        val result: Outcome<Int, String> = nested.flattenNestedFailure()
        result.shouldBe(expected = Failure(error = "deep"))
    }

    @Test
    fun `flattenNestedFailure keeps outer Success unchanged`() {
        val nested: Outcome<Int, Outcome<Int, String>> = Success(value = 99)
        val result: Outcome<Int, String> = nested.flattenNestedFailure()
        result.shouldBe(expected = Success(value = 99))
    }

    // ── flattenNestedBoth ─────────────────────────────────────────────────────

    @Test
    fun `flattenNestedBoth extracts Success from Success branch`() {
        val nested: Outcome<Outcome<Int, String>, Outcome<Int, String>> =
            Success(value = Success(value = 1))
        val result: Outcome<Int, String> = nested.flattenNestedBoth()
        result.shouldBe(expected = Success(value = 1))
    }

    @Test
    fun `flattenNestedBoth extracts Failure from Success branch`() {
        val nested: Outcome<Outcome<Int, String>, Outcome<Int, String>> =
            Success(value = Failure(error = "s-fail"))
        val result: Outcome<Int, String> = nested.flattenNestedBoth()
        result.shouldBe(expected = Failure(error = "s-fail"))
    }

    @Test
    fun `flattenNestedBoth extracts Success from Failure branch`() {
        val nested: Outcome<Outcome<Int, String>, Outcome<Int, String>> =
            Failure(error = Success(value = 2))
        val result: Outcome<Int, String> = nested.flattenNestedBoth()
        result.shouldBe(expected = Success(value = 2))
    }

    @Test
    fun `flattenNestedBoth extracts Failure from Failure branch`() {
        val nested: Outcome<Outcome<Int, String>, Outcome<Int, String>> =
            Failure(error = Failure(error = "f-fail"))
        val result: Outcome<Int, String> = nested.flattenNestedBoth()
        result.shouldBe(expected = Failure(error = "f-fail"))
    }

    // ── flatten property alias ────────────────────────────────────────────────

    @Test
    fun `flatten property alias for flattenNestedSuccess`() {
        val nested: Outcome<Outcome<Int, String>, String> = Success(value = Success(value = 5))
        val result: Outcome<Int, String> = nested.flatten
        result.shouldBe(expected = Success(value = 5))
    }
}
