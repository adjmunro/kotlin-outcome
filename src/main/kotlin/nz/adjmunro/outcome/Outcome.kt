package nz.adjmunro.outcome

import nz.adjmunro.outcome.members.errorOrNull
import nz.adjmunro.outcome.members.getOrNull

/**
 * Represents either a [Success][nz.adjmunro.outcome.Success] or [Failure][nz.adjmunro.outcome.Failure] state.
 *
 * Unlike [KotlinResult][nz.adjmunro.outcome.result.KotlinResult], [Outcome][nz.adjmunro.outcome.Outcome] carries
 * the [Error] type explicitly, so error information is never erased. Moreover, [Error] has no type restrictions.
 *
 * Use [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] to build an [Outcome][nz.adjmunro.outcome.Outcome]
 * with structured error-raising via [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope].
 *
 * @param Ok The type of the successful value.
 * @param Error The type of the error value.
 * @see nz.adjmunro.outcome.Success
 * @see nz.adjmunro.outcome.Failure
 */
public sealed interface Outcome<out Ok, out Error> {
    /**
     * Returns the [Ok] value via destructuring, or `null` if this is a
     * [Failure][nz.adjmunro.outcome.Failure]. Note that [Ok] itself may be a nullable type.
     *
     * @see nz.adjmunro.outcome.members.getOrNull
     */
    public operator fun component1(): Ok? = getOrNull()

    /**
     * Returns the [Error] value via destructuring, or `null` if this is a
     * [Success][nz.adjmunro.outcome.Success]. Note that [Error] itself may be a nullable type.
     *
     * @see nz.adjmunro.outcome.members.errorOrNull
     */
    public operator fun component2(): Error? = errorOrNull()
}
