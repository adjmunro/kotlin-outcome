package nz.adjmunro.knomadic.raise

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.catch
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.fold
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.raise
import nz.adjmunro.knomadic.raise.RaiseScope.Companion.default
import nz.adjmunro.knomadic.raise.exception.RaiseCancellationException
import nz.adjmunro.knomadic.raise.exception.RaiseScopeLeakedException
import nz.adjmunro.knomadic.util.nonFatalOrThrow
import nz.adjmunro.inline.rethrow
import kotlin.contracts.InvocationKind.AT_MOST_ONCE
import kotlin.contracts.contract

/**
 * A scope for raising errors.
 *
 * Use [default] context runner to create a new [RaiseScope]. Within the scope,
 * the [raise] method can be called to throw any [Error] as if it were a [Throwable] and
 * in doing so, short-circuit the scope execution.
 *
 * @property Error The type of error that can be raised.
 *
 * @see fold
 */
@RaiseDsl
public sealed interface RaiseScope<in Error : Any> {

    /**
     * Completes the scope, preventing any further errors from being raised.
     *
     * *This is to prevent the scope from being leaked.*
     */
    @RaiseDsl
    public fun complete()

    /**
     * Wraps [raised] as a [Throwable] and throws it, short-circuiting the [RaiseScope] execution.
     *
     * @param raised The error to raise, which can be any non-null type.
     * @return [Nothing] This function always throws.
     * @throws RaiseCancellationException When [RaiseScope] is active.
     * @throws RaiseScopeLeakedException If [RaiseScope.complete] has already been called.
     */
    @RaiseDsl
    public fun raised(error: Error): Nothing

    public class DefaultRaise<in Error : Any> @PublishedApi internal constructor() : RaiseScope<Error> {
        private val active: AtomicBoolean = atomic(initial = true)

        override fun complete() {
            active.getAndSet(value = false)
        }

        override fun raised(error: Error): Nothing {
            when (active.value) {
                true -> throw RaiseCancellationException(error)
                false -> throw RaiseScopeLeakedException()
            }
        }
    }

    public companion object {
        /**
         * A context runner use inside the [RaiseScope] when you wish to catch an expected
         * (non-fatal) [Throwable] and convert it to an [Error] from your domain.
         *
         * ```kotlin
         * val x: Faulty<String> = faultyOf {
         *     catch({ "$it" }) { throw IOException() } // raises "IOException"
         * }
         * ```
         *
         * @param catch A function that converts a [Throwable] to an [Error]. Re-throws by default.
         * @param block The block of code to execute.
         * @return The result of the block of code.
         * @throws Error If the block of code throws (provided [catch] maps [Throwable] to [Error]).
         * @throws Throwable if [catch] re-throws.
         */
        @RaiseDsl
        public inline fun <Ok, Error : Any> RaiseScope<Error>.catch(
            catch: (throwable: Throwable) -> Error = ::rethrow,
            @BuilderInference block: RaiseScope<Error>.() -> Ok,
        ): Ok {
            contract {
                callsInPlace(catch, AT_MOST_ONCE)
                callsInPlace(block, AT_MOST_ONCE)
            }

            return try { block() } catch (e: Throwable) {
                raised(error = catch(e.nonFatalOrThrow()))
            }
        }

        @RaiseDsl
        public inline fun <Ok, Error : Any> Companion.default(
            @BuilderInference action: RaiseScope<Error>.() -> Ok,
        ): Ok = with(receiver = DefaultRaise(), block = action)

        /** Ensures that the given [condition] is true, otherwise [raises][raise] an [Error]. */
        @RaiseDsl
        public inline fun <Error : Any> RaiseScope<Error>.ensure(
            condition: Boolean,
            raise: () -> Error,
        ) {
            contract { returns() implies condition }
            if (condition) raise(error = raise)
        }

        /** Ensures that the given [value] is not `null`, otherwise [raises][raise] an [Error]. */
        @RaiseDsl
        public inline fun <Ok : Any, Error : Any> RaiseScope<Error>.ensureNotNull(
            value: Ok?,
            raise: () -> Error,
        ): Ok {
            contract { returns() implies (value != null) }
            return value ?: raise(error = raise)
        }

        @RaiseDsl
        @Suppress("UNCHECKED_CAST")
        public inline fun <In, Out, Error : Any> RaiseScope<Error>.fold(
            block: (scope: RaiseScope<Error>) -> In,
            catch: (throwable: Throwable) -> Out = ::rethrow,
            recover: (error: Error) -> Out,
            transform: (value: In) -> Out,
        ): Out {
            contract {
                callsInPlace(block, AT_MOST_ONCE)
                callsInPlace(catch, AT_MOST_ONCE)
                callsInPlace(recover, AT_MOST_ONCE)
                callsInPlace(transform, AT_MOST_ONCE)
            }

            return try {
                val result = block(this@fold)
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

        /**
         * Invokes [error], wraps it as a [Throwable] and then throws it,
         * short-circuiting the [RaiseScope] execution.
         *
         * @param error The error to raise, which can be any non-null type.
         * @return [Nothing] This function always throws.
         * @throws RaiseCancellationException When [RaiseScope] is active.
         * @throws RaiseScopeLeakedException If [RaiseScope.complete] has already been called.
         * @see RaiseScope.raise
         */
        @RaiseDsl
        public inline fun <Error : Any> RaiseScope<Error>.raise(
            error: () -> Error
        ): Nothing = raised(error = error())
    }
}
