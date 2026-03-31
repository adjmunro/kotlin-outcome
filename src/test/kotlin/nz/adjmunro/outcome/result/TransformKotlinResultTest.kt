package nz.adjmunro.outcome.result

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TransformKotlinResultTest {

    private val success: KotlinResult<Int> = kotlinSuccess(value = 4)
    private val failure: KotlinResult<Int> = kotlinFailure(throwable = RuntimeException("err"))

    // ── mapFailure ────────────────────────────────────────────────────────────

    @Test
    fun `mapFailure transforms exception`() {
        val result: KotlinResult<Int> = failure.mapFailure { IllegalStateException(it.message + "!") }
        result.exceptionOrThrow().message.shouldBe(expected = "err!")
    }

    @Test
    fun `mapFailure does not affect success`() {
        success.mapFailure { RuntimeException("nope") }.getOrThrow().shouldBe(expected = 4)
    }

    @Test
    fun `mapFailure exceptions from transform propagate`() {
        shouldThrow<RuntimeException> {
            failure.mapFailure { throw RuntimeException("from-transform") }
        }
    }

    // ── flatMap ───────────────────────────────────────────────────────────────

    @Test
    fun `flatMap transforms success to new KotlinResult`() {
        val result: KotlinResult<Int> = success.flatMap { kotlinSuccess(value = it * 10) }
        result.getOrThrow().shouldBe(expected = 40)
    }

    @Test
    fun `flatMap can transform success to failure`() {
        val result: KotlinResult<Int> = success.flatMap { kotlinFailure(throwable = RuntimeException("converted")) }
        result.isFailure.shouldBe(expected = true)
    }

    @Test
    fun `flatMap leaves failure unchanged`() {
        failure.flatMap { kotlinSuccess(value = 99) }.isFailure.shouldBe(expected = true)
    }

    @Test
    fun `flatMap exceptions from transform propagate`() {
        shouldThrow<RuntimeException> {
            success.flatMap<Int, Int> { throw RuntimeException("from-transform") }
        }
    }

    // ── flatten ───────────────────────────────────────────────────────────────

    @Test
    fun `flatten unwraps inner success`() {
        val nested: KotlinResult<KotlinResult<Int>> = kotlinSuccess(value = kotlinSuccess(value = 5))
        nested.flatten().getOrThrow().shouldBe(expected = 5)
    }

    @Test
    fun `flatten unwraps inner failure`() {
        val ex: RuntimeException = RuntimeException("inner")
        val nested: KotlinResult<KotlinResult<Int>> = kotlinSuccess(value = kotlinFailure(throwable = ex))
        nested.flatten().exceptionOrThrow().shouldBe(expected = ex)
    }

    @Test
    fun `flatten on outer failure returns outer failure`() {
        val ex: RuntimeException = RuntimeException("outer")
        val nested: KotlinResult<KotlinResult<Int>> = kotlinFailure(throwable = ex)
        nested.flatten().exceptionOrThrow().shouldBe(expected = ex)
    }

    // ── collect ───────────────────────────────────────────────────────────────

    @Test
    fun `collect all successes returns Success of list`() {
        val results: List<KotlinResult<Int>> = listOf(kotlinSuccess(value = 1), kotlinSuccess(value = 2), kotlinSuccess(value = 3))
        results.collect { it.first() }.getOrThrow().shouldBe(expected = listOf(1, 2, 3))
    }

    @Test
    fun `collect with one failure invokes reduce`() {
        val ex: RuntimeException = RuntimeException("bad")
        val results: List<KotlinResult<Int>> = listOf(kotlinSuccess(value = 1), kotlinFailure(throwable = ex))
        val result: KotlinResult<List<Int>> = results.collect { errors -> errors.first() }
        result.exceptionOrThrow().shouldBe(expected = ex)
    }

    @Test
    fun `collect reduce receives all failures`() {
        val results: List<KotlinResult<Int>> = listOf(
            kotlinFailure(throwable = RuntimeException("a")),
            kotlinFailure(throwable = RuntimeException("b")),
        )
        val result: KotlinResult<List<Int>> = results.collect { errors ->
            RuntimeException(errors.joinToString { it.message ?: "" })
        }
        result.exceptionOrThrow().message.shouldBe(expected = "a, b")
    }

    @Test
    fun `collect empty list returns empty success`() {
        emptyList<KotlinResult<Int>>().collect { it.first() }.getOrThrow().shouldBe(expected = emptyList())
    }
}
