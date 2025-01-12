package nz.adjmunro.nomadic.error.util

import nz.adjmunro.nomadic.error.util.KotlinResultExt.resultOf
import nz.adjmunro.nomadic.error.util.ThrowableExt.nonFatalOrThrow
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

typealias KotlinResult<T> = kotlin.Result<T>

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
object KotlinResultExt  {

    inline infix fun <In : Any?, Out : Any?> In.resultOf( //this is andThen?
        @BuilderInference block: In.() -> Out,
    ): KotlinResult<Out> {
        contract {
            callsInPlace(block, AT_MOST_ONCE)
        }

        return try {
            KotlinResult.success(block())
        } catch (e: Throwable) {
            KotlinResult.failure(e.nonFatalOrThrow())
        }
    }

    inline infix fun <T : Any?> resultOf(
        @BuilderInference block: () -> T,
    ): KotlinResult<T> {
        contract {
            callsInPlace(block, AT_MOST_ONCE)
        }

        return try {
            KotlinResult.success(block())
        } catch (e: Throwable) {
            KotlinResult.failure(e.nonFatalOrThrow())
        }
    }

    fun t() = resultOf {
        1
    }
}

fun x() = resultOf {
    1
    val x = 1 resultOf {
        this
    } resultOf {  // daisy chaining?
        getOrThrow()
    }
}
