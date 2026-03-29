package nz.adjmunro.outcome.members

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import org.junit.jupiter.api.Test

class OutcomeFlowTest {

    private fun flow(vararg outcomes: Outcome<Int, String>): OutcomeFlow<Int, String> =
        flowOf(*outcomes)

    // ── filterOnlySuccess ─────────────────────────────────────────────────────

    @Test
    fun `filterOnlySuccess emits only success values`(): TestResult = runTest {
        val result: List<Int> = flow(Success(value = 1), Failure(error = "e"), Success(value = 2))
            .filterOnlySuccess()
            .toList()
        result.shouldBe(expected = listOf(1, 2))
    }

    @Test
    fun `filterOnlySuccess empty when all failures`(): TestResult = runTest {
        flow(Failure(error = "a"), Failure(error = "b"))
            .filterOnlySuccess()
            .toList().shouldBe(expected = emptyList())
    }

    // ── filterOnlyFailure ─────────────────────────────────────────────────────

    @Test
    fun `filterOnlyFailure emits only error values`(): TestResult = runTest {
        val result: List<String> = flow(Success(value = 1), Failure(error = "e"), Failure(error = "f"))
            .filterOnlyFailure()
            .toList()
        result.shouldBe(expected = listOf("e", "f"))
    }

    // ── onEachSuccess ─────────────────────────────────────────────────────────

    @Test
    fun `onEachSuccess invokes block for each Success and passes through all`(): TestResult = runTest {
        val collected: MutableList<Int> = mutableListOf()
        val result: List<Outcome<Int, String>> = flow(Success(value = 1), Failure(error = "e"), Success(value = 3))
            .onEachSuccess { collected += it }
            .toList()
        collected.shouldBe(expected = listOf(1, 3))
        result.shouldBe(expected = listOf(Success(value = 1), Failure(error = "e"), Success(value = 3)))
    }

    // ── onEachFailure ─────────────────────────────────────────────────────────

    @Test
    fun `onEachFailure invokes block for each Failure and passes through all`(): TestResult = runTest {
        val collected: MutableList<String> = mutableListOf()
        val result: List<Outcome<Int, String>> = flow(Success(value = 1), Failure(error = "e"), Failure(error = "f"))
            .onEachFailure { collected += it }
            .toList()
        collected.shouldBe(expected = listOf("e", "f"))
        result.size.shouldBe(expected = 3)
    }

    // ── mapSuccess ────────────────────────────────────────────────────────────

    @Test
    fun `mapSuccess transforms Success values`(): TestResult = runTest {
        flow(Success(value = 1), Failure(error = "e"), Success(value = 3))
            .mapSuccess { it * 10 }
            .toList().shouldBe(expected = listOf(Success(value = 10), Failure(error = "e"), Success(value = 30)))
    }

    // ── mapFailure ────────────────────────────────────────────────────────────

    @Test
    fun `mapFailure transforms Failure errors`(): TestResult = runTest {
        flow(Success(value = 1), Failure(error = "err"))
            .mapFailure { it.uppercase() }
            .toList().shouldBe(expected = listOf(Success(value = 1), Failure(error = "ERR")))
    }

    // ── foldOutcome ───────────────────────────────────────────────────────────

    @Test
    fun `foldOutcome maps each outcome to Output`(): TestResult = runTest {
        val result: List<String> = flow(Success(value = 1), Failure(error = "e"))
            .foldOutcome(success = { "ok:$it" }) { "err:$it" }
            .toList()
        result.shouldBe(expected = listOf("ok:1", "err:e"))
    }

    // ── collapseOutcome ───────────────────────────────────────────────────────

    @Test
    fun `collapseOutcome reduces to common ancestor`(): TestResult = runTest {
        val f: OutcomeFlow<Int, Int> = flowOf(Success(value = 1), Failure(error = 2))
        f.collapseOutcome().toList().shouldBe(expected = listOf(1, 2))
    }
}
