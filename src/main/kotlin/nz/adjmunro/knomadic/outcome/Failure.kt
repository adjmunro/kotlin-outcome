package nz.adjmunro.knomadic.outcome

/**
 * A failed [Outcome].
 *
 * @property error The error result.
 */
@OutcomeDsl
@JvmInline
public value class Failure<out Error : Any>(public val error: Error) : Outcome<Nothing, Error> {
    override fun component2(): Error = error
    override fun toString(): String = "Outcome::Failure<${error::class.simpleName}>($error)"
}
