package nz.adjmunro.knomadic.result.members

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import kotlinx.coroutines.test.runTest
import nz.adjmunro.knomadic.result.KotlinResult
import org.junit.jupiter.api.Test

internal class MapKotlinResultTest {
    @Test
    fun `mapFailure returns original on success`(): Unit = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 1)

        // When: mapFailure is called
        val mapped = result.mapFailure { Exception( "should not be called") }

        // Then: the value should remain unchanged
        mapped.getOrNull().shouldBe(1)
    }

    @Test
    fun `mapFailure transforms exception on failure`(): Unit = runTest {
        // Given: a failed result
        val ex = IllegalStateException( "fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        // When: mapFailure is called
        val mapped = result.mapFailure { IllegalArgumentException( "mapped") }

        // Then: the exception should be transformed
        mapped.exceptionOrNull().shouldBeInstanceOf<IllegalArgumentException>()
        mapped.exceptionOrNull()?.message.shouldBe("mapped")
    }

    @Test
    fun `flatMap returns transformed result on success`(): Unit = runTest {
        // Given: a successful result
        val result = KotlinResult.success(value = 2)

        // When: flatMap is called
        val mapped = result.flatMap { KotlinResult.success(value = it * 2) }

        // Then: the value should be transformed
        mapped.getOrNull().shouldBe(4)
    }

    @Test
    fun `flatMap propagates failure`(): Unit = runTest {
        // Given: a failed result
        val ex = Exception( "fail")
        val result = KotlinResult.failure<Int>(exception = ex)

        // When: flatMap is called
        val mapped = result.flatMap { KotlinResult.success(value = 99) }

        // Then: the original exception should be propagated
        mapped.exceptionOrNull().shouldBe(ex)
    }

    @Test
    fun `flatten returns inner result on success`(): Unit = runTest {
        // Given: a successful result containing another result
        val inner = KotlinResult.success(value = 5)
        val result = KotlinResult.success(value = inner)

        // When: flatten is called
        val flattened = result.flatten()

        // Then: the inner value should be returned
        flattened.getOrNull().shouldBe(5)
    }

    @Test
    fun `flatten propagates outer failure`(): Unit = runTest {
        // Given: a failed outer result
        val ex = Exception( "fail")
        val result = KotlinResult.failure<Result<Int>>(exception = ex)

        // When: flatten is called
        val flattened = result.flatten()

        // Then: the original exception should be propagated
        flattened.exceptionOrNull().shouldBe(ex)
    }
}
