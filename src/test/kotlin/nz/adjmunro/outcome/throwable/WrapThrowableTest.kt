package nz.adjmunro.outcome.throwable

import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class WrapThrowableTest {

    // ── asThrowable ───────────────────────────────────────────────────────────

    @Test
    fun `asThrowable returns Throwable as-is`() {
        val ex: RuntimeException = RuntimeException("original")
        ex.asThrowable().shouldBe(expected = ex)
    }

    @Test
    fun `asThrowable wraps null as NullPointerException`() {
        val result: Throwable = null.asThrowable { "was null" }
        result.shouldBeInstanceOf<NullPointerException>()
        result.message.shouldBe(expected = "was null")
    }

    @Test
    fun `asThrowable wraps non-throwable as IllegalStateException`() {
        val result: Throwable = "some string".asThrowable { "wrapped: $it" }
        result.shouldBeInstanceOf<IllegalStateException>()
        result.message.shouldBe(expected = "wrapped: some string")
    }

    @Test
    fun `asThrowable uses default message generator`() {
        val result: Throwable = 42.asThrowable()
        result.shouldBeInstanceOf<IllegalStateException>()
        (result.message?.contains("42")).shouldBe(expected = true)
    }

    // ── ThrowableWrapper ──────────────────────────────────────────────────────

    @Test
    fun `ThrowableWrapper cause is returned by asThrowable`() {
        val inner: RuntimeException = RuntimeException("inner")
        val wrapper: ThrowableWrapper<RuntimeException> = object : ThrowableWrapper<RuntimeException> {
            override val cause: RuntimeException = inner
        }
        wrapper.asThrowable().shouldBe(expected = inner)
    }
}
