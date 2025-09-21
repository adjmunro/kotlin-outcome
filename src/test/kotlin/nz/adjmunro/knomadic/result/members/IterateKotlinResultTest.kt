package nz.adjmunro.knomadic.result.members

import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest
import nz.adjmunro.knomadic.result.KotlinResult

internal class IterateKotlinResultTest {
    @Test
    fun `aggregate returns success when all succeed`(): Unit = runTest {
        // Given: a list of successful results
        val results = listOf(
            KotlinResult.success(value = 1),
            KotlinResult.success(value = 2),
            KotlinResult.success(value = 3)
        )

        // When: aggregate is called
        val agg = results.aggregate()

        // Then: result should be success and values should match
        agg.isSuccess.shouldBe(true)
        agg.getOrNull().shouldBe(listOf(1, 2, 3))
    }

    @Test
    fun `aggregate returns failure when any fail`(): Unit = runTest {
        // Given: a list with one failed result
        val ex = IllegalStateException( "fail")
        val results = listOf(
            KotlinResult.success(value = 1),
            KotlinResult.failure<Int>(exception = ex),
            KotlinResult.success(value = 3)
        )

        // When: aggregate is called
        val agg = results.aggregate()

        // Then: result should be failure and exception should match
        agg.isFailure.shouldBe(true)
        agg.exceptionOrNull().shouldBe(ex)
    }

    @Test
    fun `aggregate uses custom reduce for multiple failures`(): Unit = runTest {
        // Given: a list with multiple failures
        val ex1 = IllegalArgumentException( "fail1")
        val ex2 = IllegalStateException( "fail2")
        val results = listOf(
            KotlinResult.failure<Int>(exception = ex1),
            KotlinResult.failure<Int>(exception = ex2)
        )

        // When: aggregate is called with custom reduce
        val agg = results.aggregate(reduce = { Throwable(message = "combined: ${it.size}") })

        // Then: result should be failure and exception message should match
        agg.isFailure.shouldBe(true)
        agg.exceptionOrNull()?.message.shouldBe("combined: 2")
    }
}
