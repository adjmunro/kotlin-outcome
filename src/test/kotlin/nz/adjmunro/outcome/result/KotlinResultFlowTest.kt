package nz.adjmunro.outcome.result

import io.kotest.matchers.shouldBe
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Test

class KotlinResultFlowTest {

    private fun flow(results: List<Result<Int>>): KotlinResultFlow<Int> =
        results.asFlow()

    private val s1: KotlinResult<Int> = kotlinSuccess(value = 1)
    private val s2: KotlinResult<Int> = kotlinSuccess(value = 2)
    private val f1: KotlinResult<Int> = kotlinFailure(throwable = RuntimeException("a"))
    private val f2: KotlinResult<Int> = kotlinFailure(throwable = RuntimeException("b"))

    // ── filterSuccess ─────────────────────────────────────────────────────────

    @Test
    fun `filterSuccess emits only success values`(): TestResult = runTest {
        flow(listOf(s1, f1, s2)).filterSuccess().toList().shouldBe(expected = listOf(1, 2))
    }

    // ── filterFailure ─────────────────────────────────────────────────────────

    @Test
    fun `filterFailure emits only exceptions`(): TestResult = runTest {
        val result: List<Throwable> = flow(listOf(s1, f1, f2)).filterFailure().toList()
        result.map { it.message }.shouldBe(expected = listOf("a", "b"))
    }

    // ── onEachSuccess ─────────────────────────────────────────────────────────

    @Test
    fun `onEachSuccess invokes action for successes and passes through all`(): TestResult = runTest {
        val seen: MutableList<Int> = mutableListOf()
        val all: List<KotlinResult<Int>> = flow(listOf(s1, f1, s2)).onEachSuccess { seen += it }.toList()
        seen.shouldBe(expected = listOf(1, 2))
        all.size.shouldBe(expected = 3)
    }

    // ── onEachFailure ─────────────────────────────────────────────────────────

    @Test
    fun `onEachFailure invokes action for failures and passes through all`(): TestResult = runTest {
        val seen: MutableList<String?> = mutableListOf()
        val all: List<KotlinResult<Int>> = flow(listOf(s1, f1, f2)).onEachFailure { seen += it.message }.toList()
        seen.shouldBe(expected = listOf("a", "b"))
        all.size.shouldBe(expected = 3)
    }

    // ── onEachResult ──────────────────────────────────────────────────────────

    @Test
    fun `onEachResult invokes matching action for each element`(): TestResult = runTest {
        val successes: MutableList<Int> = mutableListOf()
        val failures: MutableList<String?> = mutableListOf()
        flow(listOf(s1, f1, s2)).onEachResult(
            success = { successes += it },
        ) { failures += it.message }.toList()
        successes.shouldBe(expected = listOf(1, 2))
        failures.shouldBe(expected = listOf("a"))
    }

    // ── foldResult ────────────────────────────────────────────────────────────

    @Test
    fun `foldResult maps each element`(): TestResult = runTest {
        flow(listOf(s1, f1)).foldResult(
            success = { "ok:$it" },
        ) { "err:${it.message}" }.toList().shouldBe(expected = listOf("ok:1", "err:a"))
    }

    // ── flattenResult ─────────────────────────────────────────────────────────

    @Test
    fun `flattenResult unwraps nested successes`(): TestResult = runTest {
        val nested: KotlinResultFlow<KotlinResult<Int>> = listOf(
            kotlinSuccess(value = s1),
            kotlinSuccess(value = f1),
        ).asFlow()
        val result: List<KotlinResult<Int>> = nested.flattenResult().toList()
        result[0].getOrThrow().shouldBe(expected = 1)
        result[1].isFailure.shouldBe(expected = true)
    }
}
