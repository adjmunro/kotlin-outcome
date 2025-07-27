package nz.adjmunro.knomadic.outcome

import nz.adjmunro.knomadic.KnomadicDsl
import nz.adjmunro.knomadic.KotlinResult
import nz.adjmunro.knomadic.outcome.members.errorOrNull
import nz.adjmunro.knomadic.outcome.members.getOrNull
import nz.adjmunro.knomadic.raise.RaiseScope
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.catch
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.default
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.fold
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.raise
import nz.adjmunro.inline.rethrow

/**
 * Context runner that encapsulates the [Ok] result of [block] as an [Success], and any
 * [raised][RaiseScope.raise] or [caught][RaiseScope.catch] [errors][Error] as an [Failure].
 *
 * > ***Note:** [catch] will [rethrow] by default. This is because the consumer needs to manually
 * > override the parameter and map it to an [Outcome] (if desired). Assigning it to
 * > [Failure][Failure] directly will only force [Error] to be interpreted as [Throwable]
 * > by the [RaiseScope], which may interfere with the intended [Error] type!*
 * ```kotlin
 * // Outcome<Unit, Throwable>
 * outcomeOf(::Failure) { // this: RaiseScope<Throwable> -> ... }
 *
 * // Outcome<Int, String>
 * outcomeOf { // this: RaiseScope<String> ->
 *     raise { "error" }
 *     return 3
 * }
 *
 * // Outcome<String, NullPointerException>
 * outcomeOf { // this: RaiseScope<NullPointerException> ->
 *     catch({ it }) { throw NullPointerException() }
 * }
 * ```
 *
 * @param catch Map thrown exceptions to an [Outcome]. (Throws by default).
 * @param block The code to execute.
 * @see outcome
 * @see catch
 */
@KnomadicDsl
public inline fun <Ok : Any, Error : Any> outcomeOf(
    catch: (throwable: Throwable) -> Outcome<Ok, Error> = ::rethrow,
    @BuilderInference block: RaiseScope<Error>.() -> Ok,
): Outcome<Ok, Error> {
    return RaiseScope.default {
        fold(
            block = block,
            catch = catch,
            recover = ::Failure,
            transform = ::Success,
        )
    }
}

/**
 * An alias for [outcomeOf] that uses a [String] as the [Error] type.
 *
 * > Useful for simple cases, where you fail to provide a specific error type,
 * > and just want to use any message string or [Throwable.message].
 *
 * @see outcomeOf
 * @see catch
 */
@KnomadicDsl
public inline fun <Ok : Any> outcome(
    @BuilderInference block: RaiseScope<String>.() -> Ok,
): Outcome<Ok, String> = outcomeOf(
    catch = { e: Throwable -> Failure(error = e.message ?: e.toString()) },
    block = block
)

/**
 * An alias for [outcomeOf] that uses [Throwable] as the [Error] type.
 *
 * > Useful for cases where you want to catch & wrap all exceptions to
 * > handle them as [Failure].
 *
 * @see outcomeOf
 * @see outcome
 */
@KnomadicDsl
public inline fun <Ok : Any> catch(
    @BuilderInference block: RaiseScope<Throwable>.() -> Ok,
): Outcome<Ok, Throwable> = outcomeOf(catch = ::Failure, block = block)

/**
 * Represents either a [Success] or [Failure] state.
 *
 * - Unlike [KotlinResult], [Outcome] stores the [Error] type explicitly to prevent information loss.
 * - Both [Ok] and [Error] are restricted to non-null types.
 *
 * @property Ok The type of a successful result.
 * @property Error The type of an error result.
 * @see Success
 * @see Failure
 * @see outcomeOf
 */
@KnomadicDsl
public sealed interface Outcome<out Ok : Any, out Error : Any> {

    public operator fun component1(): Ok? = getOrNull()
    public operator fun component2(): Error? = errorOrNull()
}

/**
 * A successful [Outcome].
 *
 * @property value The successful result.
 */
@KnomadicDsl
@JvmInline
public value class Success<out Ok : Any>(public val value: Ok) : Outcome<Ok, Nothing> {
    override fun component1(): Ok = value
    override fun toString(): String = "Outcome::Success<${value::class.simpleName}>($value)"
}

/**
 * A failed [Outcome].
 *
 * @property error The error result.
 */
@KnomadicDsl
@JvmInline
public value class Failure<out Error : Any>(public val error: Error) : Outcome<Nothing, Error> {
    override fun component2(): Error = error
    override fun toString(): String = "Outcome::Failure<${error::class.simpleName}>($error)"
}
