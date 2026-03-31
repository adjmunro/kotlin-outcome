package nz.adjmunro.outcome.nullable

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import org.junit.jupiter.api.Test

class BuildNullableTest {

    // ── nullableOf (suspend, cancellation-safe) ───────────────────────────────

    @Test
    fun `nullableOf returns value on success`(): TestResult = runTest {
        nullableOf { "hello" }.shouldBe(expected = "hello")
    }

    @Test
    fun `nullableOf returns null on exception`(): TestResult = runTest {
        nullableOf<String> { throw RuntimeException() }.shouldBe(expected = null)
    }

    @Test
    fun `nullableOf propagates CancellationException when coroutine is cancelled`(): TestResult = runTest {
        val job: Job = Job()
        job.cancel()
        // After cancel(), ensureActive() on the job's context throws CancellationException
        shouldThrow<CancellationException> {
            // Use withContext to run in a cancelled context
            kotlinx.coroutines.withContext(job) {
                nullableOf<String> { throw CancellationException("cancel") }
            }
        }
    }

    @Test
    fun `nullableOf swallows CancellationException when coroutine is still active`(): TestResult = runTest {
        // The coroutine is active so ensureActive() does not rethrow
        nullableOf<String> { throw CancellationException("cancel") }.shouldBe(expected = null)
    }

    // ── nullable (non-suspend) ────────────────────────────────────────────────

    @Test
    fun `nullable returns value on success`() {
        @OptIn(UnsafeForCoroutineCancellation::class)
        nullable { 42 }.shouldBe(expected = 42)
    }

    @Test
    fun `nullable returns null on exception`() {
        @OptIn(UnsafeForCoroutineCancellation::class)
        nullable<Int> { throw RuntimeException() }.shouldBe(expected = null)
    }

    @Test
    fun `nullable swallows CancellationException`() {
        // Non-suspend variant does NOT propagate CancellationException
        @OptIn(UnsafeForCoroutineCancellation::class)
        nullable<Int> { throw CancellationException() }.shouldBe(expected = null)
    }
}
