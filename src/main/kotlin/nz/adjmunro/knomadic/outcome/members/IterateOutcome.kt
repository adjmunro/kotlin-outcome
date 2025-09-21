package nz.adjmunro.knomadic.outcome.members

import nz.adjmunro.knomadic.outcome.Failure
import nz.adjmunro.knomadic.outcome.Outcome
import nz.adjmunro.knomadic.outcome.OutcomeDsl
import nz.adjmunro.knomadic.outcome.Success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * - ***If all*** [outcome][Outcome] in the [Iterable] are [success][Outcome.isSuccess],
 *   `returns` a new [Outcome] success, with a [List] of each element's encapsulated value.
 * - ***If any*** [outcome][Outcome] in the [Iterable] are [failure][Outcome.isFailure],
 *   `returns` a new [Outcome] failure with the result of the [reduce] function.
 */
@OutcomeDsl
public inline fun <Ok : Any, ErrorIn : Any, ErrorOut: Any> Iterable<Outcome<Ok, ErrorIn>>.aggregate(
    reduce: (List<ErrorIn>) -> ErrorOut,
): Outcome<List<Ok>, ErrorOut> {
    contract { callsInPlace(reduce, InvocationKind.AT_MOST_ONCE) }

    val (
        errors: List<Outcome<Ok, ErrorIn>>,
        successes: List<Outcome<Ok, ErrorIn>>,
    ) = partition(predicate = Outcome<Ok, ErrorIn>::isFailure)

    return when {
        errors.isNotEmpty() -> Failure(
            error = reduce(errors.map(transform = Outcome<Ok, ErrorIn>::errorOrThrow)),
        )

        else -> Success(
            value = successes.map(transform = Outcome<Ok, ErrorIn>::getOrThrow),
        )
    }
}
