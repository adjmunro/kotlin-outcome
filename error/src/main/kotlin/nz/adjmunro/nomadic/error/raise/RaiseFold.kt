package nz.adjmunro.nomadic.error.raise

import nz.adjmunro.nomadic.error.NomadicDsl
import nz.adjmunro.nomadic.error.raise.exception.RaiseCancellationException
import nz.adjmunro.nomadic.error.util.ThrowableExt.nonFatalOrThrow
import nz.adjmunro.nomadic.error.util.throws
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract
import kotlin.experimental.ExperimentalTypeInference

@OptIn(ExperimentalContracts::class, ExperimentalTypeInference::class)
object RaiseFold {

    @NomadicDsl
    @Suppress("UNCHECKED_CAST")
    inline fun <In : Any, Out : Any, Error : Any> fold(
        @BuilderInference block: RaiseScope<Error>.() -> In,
        @BuilderInference catch: (throwable: Throwable) -> Out,
        @BuilderInference recover: (error: Error) -> Out,
        @BuilderInference transform: (value: In) -> Out,
    ): Out {
        contract {
            callsInPlace(block, AT_MOST_ONCE)
            callsInPlace(catch, AT_MOST_ONCE)
            callsInPlace(recover, AT_MOST_ONCE)
            callsInPlace(transform, AT_MOST_ONCE)
        }

        with(RaiseScope.Default<Error>()) {
            return try {
                val result = block()
                complete()
                transform(result)
            } catch (e: RaiseCancellationException) {
                complete()
                recover(e.error as Error)
            } catch (e: Throwable) {
                complete()
                catch(e.nonFatalOrThrow())
            }
        }
    }

    @NomadicDsl
    inline fun <In : Any, Out : Any, Error : Any> (RaiseScope<Error>.() -> In).foldEager(
        @BuilderInference catch: (throwable: Throwable) -> Out = ::throws,
        @BuilderInference recover: (error: Error) -> Out,
        @BuilderInference transform: (value: In) -> Out,
    ): Out {
        contract {
            callsInPlace(this@foldEager, AT_MOST_ONCE)
        }

        return fold(
            block = { injectRaiseScope() },
            catch = catch,
            recover = recover,
            transform = transform,
        )
    }

    @NomadicDsl
    suspend fun <In : Any, Out : Any, Error : Any> (suspend RaiseScope<Error>.() -> In).foldSuspend(
        @BuilderInference catch: suspend (throwable: Throwable) -> Out = { throw it },
        @BuilderInference recover: suspend (error: Error) -> Out,
        @BuilderInference transform: suspend (value: In) -> Out,
    ): Out {
        contract {
            callsInPlace(this@foldSuspend, AT_MOST_ONCE)
            callsInPlace(catch, AT_MOST_ONCE)
            callsInPlace(recover, AT_MOST_ONCE)
            callsInPlace(transform, AT_MOST_ONCE)
        }

        return fold(
            block = { injectRaiseScope() },
            catch = { throwable -> catch(throwable) },
            recover = { error -> recover(error) },
            transform = { value -> transform(value) },
        )
    }
}
