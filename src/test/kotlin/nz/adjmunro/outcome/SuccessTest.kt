package nz.adjmunro.outcome

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class SuccessTest {

    @Test
    fun `stores value`() {
        Success(value = 42).value.shouldBe(expected = 42)
    }

    @Test
    fun `component1 returns value`() {
        val (v, _) = Success(value = "hello")
        v.shouldBe(expected = "hello")
    }

    @Test
    fun `component2 returns null`() {
        val (_, e) = Success(value = "hello")
        e.shouldBe(expected = null)
    }

    @Test
    fun `equals same value`() {
        Success(value = 1).shouldBe(expected = Success(value = 1))
    }

    @Test
    fun `not equal different value`() {
        Success(value = 1).shouldNotBe(expected = Success(value = 2))
    }

    @Test
    fun `not equal to Failure`() {
        Success(value = 1).shouldNotBe(expected = Failure(error = 1))
    }

    @Test
    fun `hashCode equal for same value`() {
        Success(value = "x").hashCode().shouldBe(expected = Success(value = "x").hashCode())
    }

    @Test
    fun `hashCode differs from Failure with same content`() {
        // Success and Failure have different hashCode salts
        Success(value = 1).hashCode().shouldNotBe(expected = Failure(error = 1).hashCode())
    }

    @Test
    fun `toString contains type and value`() {
        val s: String = Success(value = 99).toString()
        (s.contains("Success") && s.contains("99")).shouldBe(expected = true)
    }

    @Test
    fun `null value is stored`() {
        Success<String?>(value = null).value.shouldBe(expected = null)
    }

    @Test
    fun `is instance of Outcome`() {
        val o: Outcome<Int, Nothing> = Success(value = 1)
        (o is Success).shouldBe(expected = true)
    }
}
