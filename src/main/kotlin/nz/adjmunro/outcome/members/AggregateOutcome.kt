package nz.adjmunro.outcome.members

import nz.adjmunro.outcome.Failure
import nz.adjmunro.outcome.Outcome
import nz.adjmunro.outcome.Success
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Combines every [Outcome][nz.adjmunro.outcome.Outcome] in the [Iterable] into a single result.
 *
 * - If **all** elements are [Success][nz.adjmunro.outcome.Success], returns a
 *   [Success][nz.adjmunro.outcome.Success] wrapping a [List] of each element's value.
 * - If **any** element is a [Failure][nz.adjmunro.outcome.Failure], collects all errors and returns a
 *   [Failure][nz.adjmunro.outcome.Failure] produced by [reduce].
 *
 * @param reduce Combines all collected [ErrorIn] values into a single [ErrorOut] to return as the failure.
 */
public inline fun <Ok, ErrorIn, ErrorOut> Iterable<Outcome<Ok, ErrorIn>>.aggregate(
    reduce: (List<ErrorIn>) -> ErrorOut,
): Outcome<List<Ok>, ErrorOut> {
    contract { callsInPlace(lambda = reduce, kind = InvocationKind.AT_MOST_ONCE) }

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
