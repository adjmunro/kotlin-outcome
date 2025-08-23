package nz.adjmunro.knomadic.result.suspend

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nz.adjmunro.inline.TimberExtension
import nz.adjmunro.knomadic.result.KotlinResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import java.util.concurrent.atomic.AtomicInteger

@OptIn(ExperimentalCoroutinesApi::class)
@ExtendWith(TimberExtension::class)
class ParallelKotlinResultTest {
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `parallelResultOf should return all results in order`() : TestResult = runTest {
        // Given: three lambdas that return values
        val results = parallelResultOf(
            { 1 },
            { 2 },
            { 3 },
        )

        // Then: all results are present and successful
        results.map { it.getOrThrow() } shouldBe listOf(1, 2, 3)
    }

//    @Test
//    fun `Iterable parallelResult should return all results in order`() : TestResult = runTest {
//        // Given: an iterable of lambdas
//        val blocks = listOf<suspend () -> Int>(
//            suspend { 10 },
//            suspend { 20 },
//        )
//
//        // When: parallelResult is called
//        val results = blocks.parallelResult()
//
//        // Then: all results are present and successful
//        results.map { it.getOrThrow() } shouldBe listOf(10, 20)
//    }

//    @Test
//    fun `Sequence parallelResult should return all results in order`() : TestResult = runTest {
//        // Given: a sequence of lambdas
//        val blocks = sequenceOf<suspend () -> Int>(
//            { 100 },
//            { 200 },
//        ).map { suspend { it() } }
//
//        // When: parallelResult is called
//        val results = blocks.parallelResult()
//
//        // Then: all results are present and successful
//        results.map { it.getOrThrow() } shouldBe listOf(100, 200)
//    }

    @Test
    fun `parallelResultOf should capture exceptions as failures`() : TestResult = runTest {
        // Given: a lambda that throws
        val results = parallelResultOf(
            { throw IllegalStateException("fail") },
            { 42 },
        )

        // Then: first is failure, second is success
        results[0].isFailure shouldBe true
        results[1].getOrThrow() shouldBe 42
    }
}
