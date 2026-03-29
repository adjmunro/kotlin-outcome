package nz.adjmunro.outcome

import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.junit.jupiter.api.Test

class FailureTest {

    @Test
    fun `stores error`() {
        Failure(error = "err").error.shouldBe(expected = "err")
    }

    @Test
    fun `component1 returns null`() {
        val (v, _) = Failure(error = "err")
        v.shouldBe(expected = null)
    }

    @Test
    fun `component2 returns error`() {
        val (_, e) = Failure(error = "err")
        e.shouldBe(expected = "err")
    }

    @Test
    fun `equals same error`() {
        Failure(error = "x").shouldBe(expected = Failure(error = "x"))
    }

    @Test
    fun `not equal different error`() {
        Failure(error = "x").shouldNotBe(expected = Failure(error = "y"))
    }

    @Test
    fun `not equal to Success`() {
        Failure(error = 1).shouldNotBe(expected = Success(value = 1))
    }

    @Test
    fun `hashCode equal for same error`() {
        Failure(error = 42).hashCode().shouldBe(expected = Failure(error = 42).hashCode())
    }

    @Test
    fun `toString contains type and error`() {
        val s: String = Failure(error = "boom").toString()
        (s.contains("Failure") && s.contains("boom")).shouldBe(expected = true)
    }

    @Test
    fun `null error is stored`() {
        Failure<String?>(error = null).error.shouldBe(expected = null)
    }

    @Test
    fun `is instance of Outcome`() {
        val o: Outcome<Nothing, String> = Failure(error = "e")
        (o is Failure).shouldBe(expected = true)
    }
}
