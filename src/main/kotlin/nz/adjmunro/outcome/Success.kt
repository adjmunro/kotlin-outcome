package nz.adjmunro.outcome

/**
 * The successful variant of [Outcome][nz.adjmunro.outcome.Outcome].
 *
 * Carries the [value] produced by a successful operation.
 * [component1][nz.adjmunro.outcome.Outcome.component1] always returns [value];
 * [component2][nz.adjmunro.outcome.Outcome.component2] always returns `null` for a [Success][nz.adjmunro.outcome.Success].
 *
 * @property value The successful result value.
 * @see nz.adjmunro.outcome.Outcome
 * @see nz.adjmunro.outcome.Failure
 */
/* TODO: @JvmInline value class -- MockK bug fails to intercept value classes during tests. */
public class Success<out Ok>(public val value: Ok) : Outcome<Ok, Nothing> {

    override fun component1(): Ok = value

    override fun component2(): Nothing? = null

    override fun toString(): String {
        return "Outcome::Success<${value?.let { it::class.simpleName }}>($value)"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Success<*>) return false
        return value == other.value
    }

    override fun hashCode(): Int = (value?.hashCode() ?: 0) + 31 * 1 // Success tag=1

}
