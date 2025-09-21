package nz.adjmunro.knomadic.result.members

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.knomadic.result.KotlinResult
import org.junit.jupiter.api.Test
import java.io.FileNotFoundException

internal class RescopeKotlinResultTest {
    @Test
    fun `andThen transforms success value`(): Unit = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 2)

        // When: andThen is called
        val transformed = result.andThen { it * 3 }

        // Then: the value should be transformed
        transformed.getOrNull().shouldBe(6)
    }

    @Test
    fun `andThen propagates failure`(): Unit = runTest {
        // Given: a failed result
        val ex = IllegalStateException("fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        // When: andThen is called
        val transformed = result.andThen { it * 2 }

        // Then: the original exception should be propagated
        transformed.exceptionOrNull().shouldBe(ex)
    }

    @Test
    fun `andThen wraps thrown exception`(): Unit = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 1)

        // When: andThen throws an exception
        val transformed = result.andThen { throw FileNotFoundException("test") }

        // Then: the exception should be wrapped
        transformed.exceptionOrNull().shouldBeInstanceOf<FileNotFoundException>()
    }

    @Test
    fun `andIf applies transformation when predicate true`(): Unit = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 5)

        // When: andIf predicate is true
        val transformed = result.andIf(predicate = { it > 0 }, onSuccess = { it * 2 })

        // Then: the value should be transformed
        transformed.getOrNull().shouldBe(10)
    }

    @Test
    fun `andIf skips transformation when predicate false`(): Unit = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 5)

        // When: andIf predicate is false
        val transformed = result.andIf(predicate = { it < 0 }, onSuccess = { it * 2 })

        // Then: the value should remain unchanged
        transformed.getOrNull().shouldBe(5)
    }

    @Test
    fun `andIf propagates failure`(): Unit = runTest {
        // Given: a failed result
        val ex = RuntimeException("fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        // When: andIf is called
        val transformed = result.andIf(predicate = { it > 0 }, onSuccess = { it * 2 })

        // Then: the original exception should be propagated
        transformed.exceptionOrNull().shouldBe(ex)
    }

    @Test
    fun `tryRecover returns original on success`(): Unit = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 7)

        // When: tryRecover is called
        val recovered = result.tryRecover { 99 }

        // Then: the original value should be returned
        recovered.getOrNull().shouldBe(7)
    }

    fun `tryRecover recovers from failure`(): TestResult = runTest {
        val ex = IllegalArgumentException("fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        val recovered = result.tryRecover { 42 }

        recovered.getOrNull()
            .shouldBe(42)
    }

    fun `tryRecover wraps thrown exception`(): TestResult = runTest {
        val ex = IllegalStateException("fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        val recovered = result.tryRecover { throw FileNotFoundException("test") }

        recovered.exceptionOrNull()
            .shouldBeInstanceOf<FileNotFoundException>()
    }
}
