@file:Suppress("NOTHING_TO_INLINE")

package nz.adjmunro.outcome.outcome.members

import nz.adjmunro.inline.unit
import nz.adjmunro.outcome.outcome.Failure
import nz.adjmunro.outcome.outcome.Faulty
import nz.adjmunro.outcome.outcome.Maybe
import nz.adjmunro.outcome.outcome.Outcome
import nz.adjmunro.outcome.outcome.OutcomeDsl
import nz.adjmunro.outcome.outcome.Success

/** Wrap any [T] as an outcome [Success]. */
@OutcomeDsl
public inline val <T: Any> T.asSuccess: Success<T>
    get() = Success(value = this)

/** Wrap any [T] as an outcome [Failure]. */
@OutcomeDsl
public inline val <T: Any> T.asFailure: Failure<T>
    get() = Failure(error = this)

/** Convert any [Outcome] to a [Faulty] by **transforming** the [Success.value] to [Unit]. */
@OutcomeDsl
public inline val <Error : Any> Outcome<*, Error>.asFaulty: Faulty<Error>
    get() = mapSuccess(transform = ::unit)

/** Convert any [Outcome] to a [Maybe] by **transforming** the [Failure.error] to [Unit]. */
@OutcomeDsl
public inline val <Ok : Any> Outcome<Ok, *>.asMaybe: Maybe<Ok>
    get() = mapFailure(transform = ::unit)
