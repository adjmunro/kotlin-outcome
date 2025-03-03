@file:Suppress("NOTHING_TO_INLINE")
@file:OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)

package nz.adjmunro.nomadic.error.util

import nz.adjmunro.nomadic.error.NomadicDsl
import nz.adjmunro.nomadic.error.fallible.Fallible
import nz.adjmunro.nomadic.error.maybe.Maybe
import nz.adjmunro.nomadic.error.outcome.Outcome
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.InvocationKind.EXACTLY_ONCE
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@NomadicDsl
@PublishedApi
internal inline fun <T> it(value: T): T {
    return value
}

@NomadicDsl
internal inline fun <T> T.receiver(): T {
    return this@receiver
}

@NomadicDsl
internal inline fun <T> T.receiver(ignore: Any?): T {
    return this@receiver
}

@NomadicDsl
inline fun rethrow(throwable: Throwable): Nothing {
    throw throwable
}

@NomadicDsl
inline fun nulls(ignore: Any?): Unit? {
    return null
}

// this is possibly my dumbest idea yet... but i'm lazy and i want to see if it works
@NomadicDsl
inline fun <In, Out> In.nullfold(
    @BuilderInference some: (In) -> Out,
    @BuilderInference none: () -> Out,
): Out {
    contract {
        callsInPlace(some, AT_MOST_ONCE)
        callsInPlace(none, AT_MOST_ONCE)
    }

    return when (this@nullfold) {
        null -> none()
        else -> some(this@nullfold)
    }
}

@NomadicDsl
inline fun <Ok : Any> outcomeSuccess(value: Ok): Outcome.Success<Ok> {
    return Outcome.Success(value)
}

@NomadicDsl
inline fun <Error : Any> outcomeFailure(error: Error): Outcome.Failure<Error> {
    return Outcome.Failure(error)
}


@NomadicDsl
inline fun <Error : Any> outcomeFailureOf(
    @BuilderInference error: () -> Error,
): Outcome.Failure<Error> {
    contract { callsInPlace(error, EXACTLY_ONCE) }
    return Outcome.Failure(error = error())
}


@NomadicDsl
inline fun <Ok : Any> maybeSome(value: Ok): Maybe.Some<Ok> {
    return Maybe.Some(value)
}

@NomadicDsl
inline fun maybeNone(): Maybe.None {
    return Maybe.None
}

@NomadicDsl
inline fun maybeNone(ignore: Any?): Maybe.None {
    return Maybe.None
}

@NomadicDsl
inline fun falliblePass(): Fallible.Pass {
    return Fallible.Pass
}

@NomadicDsl
inline fun falliblePass(ignore: Any?): Fallible.Pass {
    return Fallible.Pass
}

@NomadicDsl
inline fun <Error : Any> fallibleOops(error: Error): Fallible.Oops<Error> {
    return Fallible.Oops(error)
}

@NomadicDsl
inline fun <Error : Any> fallibleOopsOf(
    @BuilderInference error: () -> Error,
): Fallible.Oops<Error> {
    contract { callsInPlace(error, EXACTLY_ONCE) }
    return Fallible.Oops(error())
}
