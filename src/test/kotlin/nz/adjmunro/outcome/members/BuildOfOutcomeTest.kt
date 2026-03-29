package nz.adjmunro.outcome.members

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raise
import org.junit.jupiter.api.Test

class BuildOfOutcomeTest {

    // ── outcomeOf ─────────────────────────────────────────────────────────────

    @Test
    fun `outcomeOf wraps block as Success`(): TestResult = runTest {
        outcomeOf<Int, String> { 42 }.shouldBe(expected = Success(value = 42))
    }

    @Test
    fun `outcomeOf with raise produces Failure`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf { raise { "err" } }
        result.shouldBe(expected = Failure(error = "err"))
    }

    @Test
    fun `outcomeOf default catch rethrows`(): TestResult = runTest {
        shouldThrow<RuntimeException> {
            outcomeOf<Int, String> { throw RuntimeException("boom") }
        }
    }

    @Test
    fun `outcomeOf custom catch maps exception`(): TestResult = runTest {
        val result: Outcome<Int, Exception> = outcomeOf<Int, Exception>(catch = ::Failure) {
            throw IllegalStateException("bad")
        }
        result.isFailure().shouldBe(expected = true)
    }

    @Test
    fun `outcomeOf propagates CancellationException with default catch`(): TestResult = runTest {
        shouldThrow<CancellationException> {
            outcomeOf<Int, Exception> { throw CancellationException("cancel") }
        }
    }

    @Test
    fun `outcomeOf custom catch wraps CancellationException when coroutine active`(): TestResult = runTest {
        // Coroutine is active, so ensureActive() does not throw;
        // the exception is passed to the catch lambda.
        val result: Outcome<Int, Exception> = outcomeOf<Int, Exception>(
            catch = { Failure(error = it as Exception) },
        ) { throw CancellationException("not actually cancelled") }
        result.isFailure().shouldBe(expected = true)
    }

    // ── faultOf ───────────────────────────────────────────────────────────────

    @Test
    fun `faultOf returns emptySuccess when block completes`(): TestResult = runTest {
        faultOf<String> { /* nothing */ }.shouldBe(expected = emptySuccess())
    }

    @Test
    fun `faultOf with raise returns Failure`(): TestResult = runTest {
        faultOf<String> { raise { "e" } }.shouldBe(expected = Failure(error = "e"))
    }

    @Test
    fun `faultOf propagates CancellationException`(): TestResult = runTest {
        shouldThrow<CancellationException> {
            faultOf<String> { throw CancellationException() }
        }
    }

    // ── maybeOf ───────────────────────────────────────────────────────────────

    @Test
    fun `maybeOf wraps value as Success`(): TestResult = runTest {
        maybeOf<Int> { 7 }.shouldBe(expected = Success(value = 7))
    }

    @Test
    fun `maybeOf with raise returns emptyFailure`(): TestResult = runTest {
        maybeOf<Int> { raise { Unit } }.shouldBe(expected = emptyFailure())
    }

    @Test
    fun `maybeOf default catch returns emptyFailure on exception`(): TestResult = runTest {
        maybeOf<Int> { throw RuntimeException() }.shouldBe(expected = emptyFailure())
    }

    @Test
    fun `maybeOf swallows CancellationException via default catch when coroutine active`(): TestResult = runTest {
        // maybeOf default catch = ::emptyFailure, which receives the CancellationException
        // after ensureActive() confirms the coroutine is still running.
        maybeOf<Int> { throw CancellationException() }.shouldBe(expected = emptyFailure())
    }

    // ── catchExceptionOf / catchStringOf ──────────────────────────────────────

    @Test
    fun `catchExceptionOf wraps success`(): TestResult = runTest {
        catchExceptionOf<Int> { 3 }.shouldBe(expected = Success(value = 3))
    }

    @Test
    fun `catchExceptionOf wraps exception as Failure`(): TestResult = runTest {
        val ex: RuntimeException = RuntimeException("e")
        catchExceptionOf<Int> { throw ex }.shouldBe(expected = Failure(error = ex))
    }

    @Test
    fun `catchStringOf wraps success`(): TestResult = runTest {
        catchStringOf<Int> { 9 }.shouldBe(expected = Success(value = 9))
    }

    @Test
    fun `catchStringOf converts exception to Failure of message`(): TestResult = runTest {
        catchStringOf<Int> { throw RuntimeException("msg") }.shouldBe(expected = Failure(error = "msg"))
    }
}
