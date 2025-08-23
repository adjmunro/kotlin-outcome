package nz.adjmunro.knomadic.result.suspend

import io.kotest.matchers.shouldBe
import io.mockk.coVerify
import io.mockk.mockk
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
import nz.adjmunro.knomadic.result.ResultFlow
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ConditionalResultFlowTest {
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `filterSuccess should emit only successful values`() : TestResult = runTest {
        // Given: a flow with both success and failure results
        val flow = flowOf(
            KotlinResult.success(1),
            KotlinResult.failure<Int>(Exception("fail")),
            KotlinResult.success(2),
        )

        // When: filterSuccess is called
        val result = flow.filterSuccess().toList()

        // Then: only successful values are emitted
        result shouldBe listOf(1, 2)
    }

    @Test
    fun `filterFailure should emit only failure exceptions`() : TestResult = runTest {
        // Given: a flow with both success and failure results
        val ex = Exception("fail")
        val flow = flowOf(
            KotlinResult.success(1),
            KotlinResult.failure<Int>(ex),
        )

        // When: filterFailure is called
        val result = flow.filterFailure().toList()

        // Then: only the exception is emitted
        result shouldBe listOf(ex)
    }

    @Test
    fun `onEachSuccess should call action for each success`() : TestResult = runTest {
        // Given: a flow with both success and failure results
        val action = mockk<suspend (Int) -> Unit>(relaxed = true)
        val flow = flowOf(
            KotlinResult.success(1),
            KotlinResult.failure<Int>(Exception()),
            KotlinResult.success(2),
        )

        // When: onEachSuccess is called
        flow.onEachSuccess(action).toList()

        // Then: action is called for each success
        coVerify(exactly = 1) { action(1) }
        coVerify(exactly = 1) { action(2) }
    }
}
