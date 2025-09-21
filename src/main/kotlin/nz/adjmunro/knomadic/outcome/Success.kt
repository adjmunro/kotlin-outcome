package nz.adjmunro.knomadic.outcome

/**
 * A successful [Outcome].
 *
 * @property value The successful result.
 */
@OutcomeDsl
@JvmInline
public value class Success<out Ok : Any>(public val value: Ok) : Outcome<Ok, Nothing> {
    override fun component1(): Ok = value
    override fun toString(): String = "Outcome::Success<${value::class.simpleName}>($value)"
}
