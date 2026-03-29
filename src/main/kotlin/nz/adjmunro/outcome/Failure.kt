package nz.adjmunro.outcome

/**
 * The failed variant of [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * Carries the [error] value that describes why the operation did not succeed.
 * [component1][nz.adjmunro.outcome.Outcome.component1] always returns `null` for a [Failure][nz.adjmunro.outcome.Failure];
 * [component2][nz.adjmunro.outcome.Outcome.component2] always returns [error].
 *
 * @property error The error value describing the failure.
 * @see nz.adjmunro.outcome.Outcome
 * @see nz.adjmunro.outcome.Success
 */
/* TODO: @JvmInline value class -- MockK bug fails to intercept value classes during tests. */
public class Failure<out Error>(public val error: Error) : Outcome<Nothing, Error> {

    override fun component1(): Nothing? = null

    override fun component2(): Error = error

    override fun toString(): String {
        return "Outcome::Failure<${error?.let { it::class.simpleName }}>($error)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Failure<*>) return false
        return error == other.error
    }

    override fun hashCode(): Int = (error?.hashCode() ?: 0) + 31 * 2 // Failure tag=2
}
