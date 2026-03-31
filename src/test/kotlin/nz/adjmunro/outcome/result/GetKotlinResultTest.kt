package nz.adjmunro.outcome.result

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class GetKotlinResultTest {

    private val success: KotlinResult<Int> = kotlinSuccess(value = 42)
    private val failure: KotlinResult<Int> = kotlinFailure(throwable = RuntimeException("err"))

    // ── exceptionOrThrow ──────────────────────────────────────────────────────

    @Test
    fun `exceptionOrThrow returns exception for failure`() {
        failure.exceptionOrThrow().message.shouldBe(expected = "err")
    }

    @Test
    fun `exceptionOrThrow throws NoSuchElementException for success`() {
        shouldThrow<NoSuchElementException> { success.exceptionOrThrow() }
    }

    // ── exceptionOrElse ───────────────────────────────────────────────────────

    @Test
    fun `exceptionOrElse returns exception for failure`() {
        failure.exceptionOrElse { RuntimeException("default") }.message.shouldBe(expected = "err")
    }

    @Test
    fun `exceptionOrElse invokes onSuccess for success`() {
        val result: Throwable = success.exceptionOrElse { IllegalStateException("from:$it") }
        result.shouldBeInstanceOf<IllegalStateException>()
        result.message.shouldBe(expected = "from:42")
    }

    // ── exceptionOrDefault ────────────────────────────────────────────────────

    @Test
    fun `exceptionOrDefault returns exception for failure`() {
        failure.exceptionOrDefault(defaultError = RuntimeException("default")).message.shouldBe(expected = "err")
    }

    @Test
    fun `exceptionOrDefault returns default for success`() {
        val default: RuntimeException = RuntimeException("default")
        success.exceptionOrDefault(defaultError = default).shouldBe(expected = default)
    }
}
