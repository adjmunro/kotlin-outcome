package nz.adjmunro.knomadic.result.members

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import nz.adjmunro.knomadic.result.members.resultOf
import org.junit.jupiter.api.Test
import kotlinx.coroutines.test.runTest

internal class BuildKotlinResultTest {
    @Test
    fun `resultOf returns success for normal block`(): Unit = runTest {
        // Given: an expected value
        val expected = 42

        // When: resultOf is called with a normal block
        val result = resultOf(block = { expected })

        // Then: result should be success and value should match expected
        result.isSuccess.shouldBe(true)
        result.getOrNull().shouldBe(expected)
    }

    @Test
    fun `resultOf returns failure for non-fatal exception`(): Unit = runTest {
        // Given: a non-fatal exception
        val exception = IllegalArgumentException("fail")

        // When: resultOf is called with a block that throws
        val result = resultOf(block = { throw exception })

        // Then: result should be failure and exception should match
        result.isFailure.shouldBe(true)
        result.exceptionOrNull().shouldBe(exception)
    }

    @Test
    fun `resultOf rethrows fatal exception`(): Unit = runTest {
        // Given: a fatal exception
        val exception = CancellationException("fatal")

        // When: resultOf is called with a block that throws fatal exception
        val thrown = runCatching {
            resultOf(block = { throw exception })
        }.exceptionOrNull()

        // Then: the fatal exception should be rethrown
        thrown.shouldBe(exception)
    }

    @Test
    fun `receiver resultOf returns success for normal block`(): Unit = runTest {
        // Given: a receiver value
        val receiver = "receiver"
        val expected = "receiver-success"

        // When: receiver resultOf is called
        val result = receiver.resultOf(block = { "$this-success" })

        // Then: result should be success and value should match expected
        result.isSuccess.shouldBe(true)
        result.getOrNull().shouldBe(expected)
    }

    @Test
    fun `receiver resultOf returns failure for non-fatal exception`(): Unit = runTest {
        // Given: a receiver value and a non-fatal exception
        val receiver = "receiver"
        val exception = IllegalStateException("fail")

        // When: receiver resultOf is called with a block that throws
        val result = receiver.resultOf(block = { throw exception })

        // Then: result should be failure and exception should match
        result.isFailure.shouldBe(true)
        result.exceptionOrNull().shouldBe(exception)
    }

    @Test
    fun `receiver resultOf rethrows fatal exception`(): Unit = runTest {
        // Given: a receiver value and a fatal exception
        val receiver = "receiver"
        val exception = CancellationException("fatal")

        // When: receiver resultOf is called with a block that throws fatal exception
        val thrown = runCatching { receiver.resultOf(block = { throw exception }) }.exceptionOrNull()

        // Then: the fatal exception should be rethrown
        thrown.shouldBe(exception)
    }
}
