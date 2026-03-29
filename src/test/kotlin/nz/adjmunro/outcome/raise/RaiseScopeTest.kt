package nz.adjmunro.outcome.raise

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.test.TestResult
import kotlinx.coroutines.test.runTest
import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.members.outcome
import nz.adjmunro.outcome.members.outcomeOf
import nz.adjmunro.outcome.raise.RaiseScope.Companion.catch
import nz.adjmunro.outcome.raise.RaiseScope.Companion.catching
import nz.adjmunro.outcome.raise.RaiseScope.Companion.ensure
import nz.adjmunro.outcome.raise.RaiseScope.Companion.ensureInstanceOf
import nz.adjmunro.outcome.raise.RaiseScope.Companion.ensureNotNull
import nz.adjmunro.outcome.raise.RaiseScope.Companion.fold
import nz.adjmunro.outcome.raise.RaiseScope.Companion.folding
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raise
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raises
import org.junit.jupiter.api.Test

@OptIn(UnsafeForCoroutineCancellation::class)
class RaiseScopeTest {

    // ── DefaultRaiseScope lifecycle ───────────────────────────────────────────

    @Test
    fun `shortCircuit throws RaisedException when scope active`() {
        val scope: DefaultRaiseScope<String> = DefaultRaiseScope()
        shouldThrow<RaisedException> { scope.shortCircuit(error = "err") }
    }

    @Test
    fun `shortCircuit throws RaiseScopeLeakedException after close`() {
        val scope: DefaultRaiseScope<String> = DefaultRaiseScope()
        scope.close()
        shouldThrow<RaiseScopeLeakedException> { scope.shortCircuit(error = "err") }
    }

    @Test
    fun `close is idempotent — second close does not throw`() {
        val scope: DefaultRaiseScope<String> = DefaultRaiseScope()
        scope.close()
        scope.close() // should not throw
    }

    // ── raise ─────────────────────────────────────────────────────────────────

    @Test
    fun `raise via outcomeOf produces Failure`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf { raise { "oops" } }
        result.shouldBe(expected = Failure(error = "oops"))
    }

    @Test
    fun `raise is equivalent to shortCircuit`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf { shortCircuit(error = "err") }
        result.shouldBe(expected = Failure(error = "err"))
    }

    // ── ensure ────────────────────────────────────────────────────────────────

    @Test
    fun `ensure does nothing when condition true`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf {
            ensure(condition = true) { "should not raise" }
            42
        }
        result.shouldBe(expected = Success(value = 42))
    }

    @Test
    fun `ensure raises when condition false`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf {
            ensure(condition = false) { "failed" }
            42
        }
        result.shouldBe(expected = Failure(error = "failed"))
    }

    // ── ensureNotNull ─────────────────────────────────────────────────────────

    @Test
    fun `ensureNotNull returns value when non-null`(): TestResult = runTest {
        val result: Outcome<String, String> = outcomeOf {
            ensureNotNull(value = "hello") { "was null" }
        }
        result.shouldBe(expected = Success(value = "hello"))
    }

    @Test
    fun `ensureNotNull raises when null`(): TestResult = runTest {
        val result: Outcome<String, String> = outcomeOf {
            ensureNotNull(value = null) { "was null" }
        }
        result.shouldBe(expected = Failure(error = "was null"))
    }

    // ── ensureInstanceOf ──────────────────────────────────────────────────────

    @Test
    fun `ensureInstanceOf returns value when correct type`(): TestResult = runTest {
        val any: Any = "hello"
        val result: Outcome<String, String> = outcomeOf {
            ensureInstanceOf<Any, String, String>(check = any, isType = String::class)
        }
        result.shouldBe(expected = Success(value = "hello"))
    }

    @Test
    fun `ensureInstanceOf raises when wrong type`(): TestResult = runTest {
        val any: Any = 42
        val result: Outcome<String, String> = outcomeOf {
            ensureInstanceOf<Any, String, String>(
                check = any,
                isType = String::class,
            ) { "not a string" }
        }
        result.shouldBe(expected = Failure(error = "not a string"))
    }

    // ── catching (suspend, coroutine-safe) ────────────────────────────────────

    @Test
    fun `catching maps exception to Error`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf {
            catching(catch = { e -> e.message!! }) { throw RuntimeException("caught") }
        }
        result.shouldBe(expected = Failure(error = "caught"))
    }

    @Test
    fun `catching propagates CancellationException when coroutine active`(): TestResult = runTest {
        shouldThrow<CancellationException> {
            outcomeOf<Int, String> {
                catching { throw CancellationException("cancel") }
            }
        }
    }

    // ── catch (non-suspend, unsafe) ───────────────────────────────────────────

    @Test
    fun `catch maps exception to Error`() {
        val result: Outcome<Int, String> = outcome {
            catch(catch = { e -> e.message!! }) { throw RuntimeException("boom") }
        }
        result.shouldBe(expected = Failure(error = "boom"))
    }

    // ── fold / folding ────────────────────────────────────────────────────────

    @Test
    fun `fold executes transform on success`() {
        val scope: DefaultRaiseScope<String> = DefaultRaiseScope()
        val result: String = scope.run {
            fold(
                block = { 42 },
                recover = { "fail" },
            ) { "ok:$it" }
        }
        result.shouldBe(expected = "ok:42")
    }

    @Test
    fun `fold executes recover on raised error`() {
        val scope: DefaultRaiseScope<String> = DefaultRaiseScope()
        val result: String = scope.run {
            fold(
                block = { raise { "err" } },
                recover = { "recovered:$it" },
            ) { "ok" }
        }
        result.shouldBe(expected = "recovered:err")
    }

    @Test
    fun `folding suspend variant works correctly`(): TestResult = runTest {
        val scope: DefaultRaiseScope<String> = DefaultRaiseScope()
        val result: String = scope.run {
            folding(
                block = { 7 },
                recover = { "fail" },
            ) { "ok:$it" }
        }
        result.shouldBe(expected = "ok:7")
    }

    // ── raises / nothing type-hint helpers ────────────────────────────────────

    @Test
    fun `raises returns same scope`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf {
            raises<String>()
            42
        }
        result.shouldBe(expected = Success(value = 42))
    }

    @Test
    fun `raises with type value returns same scope`(): TestResult = runTest {
        val result: Outcome<Int, String> = outcomeOf {
            raises(type = "hint")
            10
        }
        result.shouldBe(expected = Success(value = 10))
    }
}
