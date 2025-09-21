package nz.adjmunro.knomadic.result.members

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.knomadic.result.KotlinResult
import org.junit.jupiter.api.Test

internal class GetKotlinResultTest {
    @Test
    fun `exceptionOrThrow throws on success`(): TestResult = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 1)

        // When: exceptionOrThrow is called
        val exception = runCatching {
            result.exceptionOrThrow()
        }.exceptionOrNull()

        // Then: NoSuchElementException should be thrown
        exception.shouldBeInstanceOf<NoSuchElementException>()
    }

    @Test
    fun `exceptionOrThrow returns exception on failure`(): TestResult = runTest {
        // Given: a failed result
        val ex = IllegalStateException("fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        // When: exceptionOrThrow is called
        val thrown = result.exceptionOrThrow()

        // Then: the original exception should be returned
        thrown.shouldBe(ex)
    }

    @Test
    fun `exceptionOrElse returns transformed exception on success`(): TestResult = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 2)
        val ex = IllegalArgumentException("custom")

        // When: exceptionOrElse is called
        val transformed = result.exceptionOrElse { ex }

        // Then: the transformed exception should be returned
        transformed.shouldBe(ex)
    }

    @Test
    fun `exceptionOrElse returns original exception on failure`(): TestResult = runTest {
        // Given: a failed result
        val ex = RuntimeException("fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        // When: exceptionOrElse is called
        val transformed = result.exceptionOrElse { IllegalArgumentException() }

        // Then: the original exception should be returned
        transformed.shouldBe(ex)
    }

    @Test
    fun `exceptionOrDefault returns default on success`(): TestResult = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 3)
        val ex = Exception("default")

        // When: exceptionOrDefault is called
        val defaulted = result.exceptionOrDefault(default = ex)

        // Then: the default exception should be returned
        defaulted.shouldBe(ex)
    }

    @Test
    fun `exceptionOrDefault returns original exception on failure`(): TestResult = runTest {
        // Given: a failed result
        val ex = Exception("fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        // When: exceptionOrDefault is called
        val defaulted = result.exceptionOrDefault(default = Exception("unused"))

        // Then: the original exception should be returned
        defaulted.shouldBe(ex)
    }
}
