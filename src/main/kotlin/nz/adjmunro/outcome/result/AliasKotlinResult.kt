@file:Suppress("unused", "NOTHING_TO_INLINE")

package nz.adjmunro.outcome.result

import kotlinx.coroutines.flow.Flow
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Typealias for [kotlin.Result], disambiguating it from other result-type classes in the same codebase.
 */
public typealias KotlinResult<T> = Result<T>

/**
 * Typealias for a [Flow][kotlinx.coroutines.flow.Flow] of
 * [KotlinResult][nz.adjmunro.outcome.result.KotlinResult] values.
 *
 * @param Ok The success value type of each result in the flow.
 * @see nz.adjmunro.outcome.result.KotlinResult
 */
public typealias KotlinResultFlow<Ok> = Flow<KotlinResult<Ok>>

/** Wraps [value] as a [Result.success][kotlin.Result.success]. */
public inline fun <T> kotlinSuccess(value: T): KotlinResult<T> {
    return Result.success(value = value)
}

/** Wraps the result of [block] as a [Result.success][kotlin.Result.success]. */
public inline fun <T> kotlinSuccess(block: () -> T): KotlinResult<T> {
    contract { callsInPlace(lambda = block, kind = InvocationKind.EXACTLY_ONCE) }
    return Result.success(value = block())
}

/** Wraps the result of [block] as a [Result.success][kotlin.Result.success]. */
public inline fun <In, Out> In.kotlinSuccess(block: In.() -> Out): KotlinResult<Out> {
    contract { callsInPlace(lambda = block, kind = InvocationKind.EXACTLY_ONCE) }
    return Result.success(value = block(this@kotlinSuccess))
}

/** Wraps [throwable] as a [Result.failure][kotlin.Result.failure]. */
public inline fun kotlinFailure(throwable: Throwable): KotlinResult<Nothing> {
    return Result.failure(exception = throwable)
}

/** Wraps the result of [block] as a [Result.failure][kotlin.Result.failure]. */
public inline fun kotlinFailure(block: () -> Throwable): KotlinResult<Nothing> {
    contract { callsInPlace(lambda = block, kind = InvocationKind.EXACTLY_ONCE) }
    return Result.failure(exception = block())
}
