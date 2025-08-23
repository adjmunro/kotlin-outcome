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
class FoldResultFlowTest {
    @BeforeEach
    fun setup() {
        Dispatchers.setMain(StandardTestDispatcher())
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun `foldResult should map success and failure correctly`() : TestResult = runTest {
        // Given: a flow with both success and failure results
        val flow = flowOf(
            KotlinResult.success(1),
            KotlinResult.failure<Int>(Exception("fail")),
        )

        // When: foldResult is called
        val result = flow.foldResult(
            success = { it + 1 },
            failure = { -1 },
        ).toList()

        // Then: success is mapped, failure is mapped
        result shouldBe listOf(2, -1)
    }

    @Test
    fun `flatten should flatten nested KotlinResult`() : TestResult = runTest {
        // Given: a flow of nested KotlinResult
        val flow = flowOf(
            KotlinResult.success(KotlinResult.success(1)),
            KotlinResult.success(KotlinResult.failure<Int>(Exception("fail"))),
        )

        // When: flatten is called
        val result = flow.flatten().toList()

        // Then: the result is flattened
        result[0].getOrThrow() shouldBe 1
        result[1].isFailure shouldBe true
    }
}
