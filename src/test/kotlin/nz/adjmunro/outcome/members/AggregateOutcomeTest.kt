package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class AggregateOutcomeTest {

    @Test
    fun `aggregate of all successes returns Success of list`() {
        val outcomes: List<Outcome<Int, String>> = listOf(Success(value = 1), Success(value = 2), Success(value = 3))
        outcomes.aggregate { errors -> errors.joinToString() }.shouldBe(expected = Success(value = listOf(1, 2, 3)))
    }

    @Test
    fun `aggregate with single failure returns Failure`() {
        val outcomes: List<Outcome<Int, String>> = listOf(Success(value = 1), Failure(error = "bad"), Success(value = 3))
        val result: Outcome<List<Int>, String> = outcomes.aggregate { it.first() }
        result.shouldBe(expected = Failure(error = "bad"))
    }

    @Test
    fun `aggregate reduce receives all errors`() {
        val outcomes: List<Outcome<Int, String>> = listOf(Failure(error = "a"), Failure(error = "b"), Success(value = 1))
        val result: Outcome<List<Int>, String> = outcomes.aggregate { errors ->
            errors.joinToString(",")
        }
        result.shouldBe(expected = Failure(error = "a,b"))
    }

    @Test
    fun `aggregate on empty list returns empty Success`() {
        val outcomes: List<Outcome<Int, String>> = emptyList()
        outcomes.aggregate { errors -> errors.joinToString() }.shouldBe(expected = Success(value = emptyList()))
    }

    @Test
    fun `aggregate preserves success order`() {
        val outcomes: List<Outcome<Int, String>> = listOf(Success(value = 3), Success(value = 1), Success(value = 2))
        (outcomes.aggregate { it.first() } as Success).value.shouldBe(expected = listOf(3, 1, 2))
    }
}
