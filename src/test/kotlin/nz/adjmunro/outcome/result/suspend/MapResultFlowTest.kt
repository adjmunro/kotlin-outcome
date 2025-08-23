package nz.adjmunro.knomadic.result.suspend

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import nz.adjmunro.knomadic.result.KotlinResult
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class MapResultFlowTest {
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `mapSuccess should map only success values`() : TestResult = runTest {
        // Given: a flow with both success and failure results
        val flow = flowOf(
            KotlinResult.success(1),
            KotlinResult.failure<Int>(Exception()),
        )

        // When: mapSuccess is called
        val result = flow.mapSuccess { it + 1 }.toList()

        // Then: only success is mapped, failure is unchanged
        result[0].getOrThrow() shouldBe 2
        result[1].isFailure shouldBe true
    }

    @Test
    fun `andThen should chain success values`() : TestResult = runTest {
        // Given: a flow with both success and failure results
        val flow = flowOf(
            KotlinResult.success(1),
            KotlinResult.failure<Int>(Exception()),
        )

        // When: andThen is called
        val result = flow.andThen { it + 1 }.toList()

        // Then: only success is mapped, failure is unchanged
        result[0].getOrThrow() shouldBe 2
        result[1].isFailure shouldBe true
    }

    @Test
    fun `andThen with predicate should map only if predicate true`() : TestResult = runTest {
        // Given: a flow with both success and failure results
        val flow = flowOf(
            KotlinResult.success(2),
            KotlinResult.success(1),
        )

        // When: andThen with predicate is called
        val result = flow.andThen(
            predicate = { it > 1 },
            success = { it * 2 },
        ).toList()

        // Then: only values matching predicate are mapped
        result[0].getOrThrow() shouldBe 4
        result[1].getOrThrow() shouldBe 1
    }
}
