@file:Suppress("NOTHING_TO_INLINE")

package nz.adjmunro.outcome.raise

import nz.adjmunro.outcome.raise.RaiseScope.Companion.catching
import nz.adjmunro.outcome.raise.RaiseScope.Companion.folding
import nz.adjmunro.outcome.raise.RaiseScope.Companion.raise
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
import nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
import nz.adjmunro.outcome.inline.rethrow
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract
import kotlin.reflect.KClass

/**
 * Shorthand for a lambda that produces [Ok] or short-circuits with an [Error] inside a
 * [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope].
 *
 * @param Ok The success value type.
 * @param Error The error type that can be raised.
 */
public typealias Raises<Ok, Error> = RaiseScope<Error>.() -> Ok

/**
 * A scope for raising typed errors without exceptions leaking to callers.
 *
 * Create a new scope with [DefaultRaiseScope][nz.adjmunro.outcome.raise.DefaultRaiseScope], or use a builder
 * like [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] / [faultOf][nz.adjmunro.outcome.members.faultOf].
 * Inside the scope, call [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] to short-circuit execution with an
 * [Error]. The scope's [fold][nz.adjmunro.outcome.raise.RaiseScope.fold] /
 * [folding][nz.adjmunro.outcome.raise.RaiseScope.folding] captures the raised error and maps it to an output value.
 *
 * @param Error The type of error that can be raised within this scope.
 *
 * @see nz.adjmunro.outcome.raise.RaiseScope.folding
 */
public interface RaiseScope<in Error> {

    /**
     * Throws the given [error], immediately short-circuiting the
     * [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] execution.
     *
     * The thrown exception is caught by [fold][nz.adjmunro.outcome.raise.RaiseScope.fold] /
     * [folding][nz.adjmunro.outcome.raise.RaiseScope.folding] and passed to their `recover` lambda.
     * Prefer calling [raise][nz.adjmunro.outcome.raise.RaiseScope.raise] at the call site — it is the
     * inline wrapper intended for direct use.
     *
     * @param error The error value to raise.
     * @return [Nothing] — always throws.
     * @throws RaisedException [nz.adjmunro.outcome.raise.RaisedException] while the scope is active.
     * @throws RaiseScopeLeakedException [nz.adjmunro.outcome.raise.RaiseScopeLeakedException] if
     *   [close][nz.adjmunro.outcome.raise.RaiseScope.close] has already been called.
     */
    public fun shortCircuit(error: Error): Nothing

    /**
     * Marks the scope as expired.
     *
     * After `close()`, any call to [shortCircuit][nz.adjmunro.outcome.raise.RaiseScope.shortCircuit] throws
     * [RaiseScopeLeakedException][nz.adjmunro.outcome.raise.RaiseScopeLeakedException] instead of the normal
     * [RaisedException][nz.adjmunro.outcome.raise.RaisedException]. Called automatically by
     * [fold][nz.adjmunro.outcome.raise.RaiseScope.fold] /
     * [folding][nz.adjmunro.outcome.raise.RaiseScope.folding] when the block finishes.
     */
    public fun close()

    public companion object {
        /**
         * Executes [block] inside the current [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope], converting any
         * thrown [Throwable] — including fatal [kotlin.Error] subclasses — to a raised [Error] via [catch].
         *
         * Unlike [catch][nz.adjmunro.outcome.raise.RaiseScope.catch], this suspend variant calls
         * [ensureActive][kotlinx.coroutines.ensureActive] before delegating to [catch], so
         * [CancellationException][kotlinx.coroutines.CancellationException] is never silently swallowed.
         * Prefer `catching` over `catch` in suspend contexts.
         *
         * ***It is YOUR responsibility to use [catch] to handle fatal [kotlin.Error] cases appropriately!***
         *
         * ```kotlin
         * val x: Fault<String> = faultOf {
         *     catching({ "$it" }) { throw IOException() } // raises "IOException"
         * }
         * ```
         *
         * @param catch Converts a caught [Throwable] to an [Error]. Re-throws by default.
         * @param block The suspend block to execute.
         * @return The result of [block] on success.
         * @throws Throwable If [catch] re-throws.
         * @see nz.adjmunro.outcome.raise.RaiseScope.catch
         */
        @EnsuresActiveCoroutine
        public suspend inline fun <Ok, Error> RaiseScope<Error>.catching(
            catch: (throwable: Throwable) -> Error = ::rethrow,
            block: RaiseScope<Error>.() -> Ok,
        ): Ok {
            contract {
                callsInPlace(lambda = catch, kind = InvocationKind.AT_MOST_ONCE)
                callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE)
            }

            @OptIn(UnsafeForCoroutineCancellation::class)
            return catch(
                catch = { e: Throwable ->
                    if (e is CancellationException) {
                        currentCoroutineContext().ensureActive()
                    }
                    catch(e)
                },
                block = block,
            )
        }

        /**
         * Executes [block] inside the current [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope], converting any
         * thrown [Throwable] — including fatal [kotlin.Error] subclasses — to a raised [Error] via [catch].
         *
         * ***Warning:** [CancellationException][kotlinx.coroutines.CancellationException] is not propagated.
         * Use [catching][nz.adjmunro.outcome.raise.RaiseScope.catching] in suspend contexts.*
         *
         * ***It is YOUR responsibility to use [catch] to handle fatal [kotlin.Error] cases appropriately!***
         *
         * ```kotlin
         * val x: Fault<String> = fault {
         *     catch({ "$it" }) { throw IOException() } // raises "IOException"
         * }
         * ```
         *
         * @param catch Converts a caught [Throwable] to an [Error]. Re-throws by default.
         * @param block The block to execute.
         * @return The result of [block] on success.
         * @throws Throwable If [catch] re-throws.
         * @see nz.adjmunro.outcome.raise.RaiseScope.catching
         */
        @UnsafeForCoroutineCancellation
        public inline fun <Ok, Error> RaiseScope<Error>.catch(
            catch: (throwable: Throwable) -> Error = ::rethrow,
            block: RaiseScope<Error>.() -> Ok,
        ): Ok {
            contract {
                callsInPlace(lambda = catch, kind = InvocationKind.AT_MOST_ONCE)
                callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE)
            }

            return try { block() } catch (e: Throwable) {
                shortCircuit(error = catch(e))
            }
        }

        /**
         * Asserts [condition] is `true`, otherwise
         * [raises][nz.adjmunro.outcome.raise.RaiseScope.raise] the [Error] produced by [raise].
         */
        public inline fun <Error> RaiseScope<Error>.ensure(
            condition: Boolean,
            raise: () -> Error,
        ) {
            contract { returns() implies condition }
            if (!condition) raise(error = raise)
        }

        /**
         * Returns [value] if it is non-null, otherwise
         * [raises][nz.adjmunro.outcome.raise.RaiseScope.raise] the [Error] produced by [raise].
         */
        public inline fun <Ok, Error> RaiseScope<Error>.ensureNotNull(
            value: Ok?,
            raise: () -> Error,
        ): Ok & Any {
            contract { returns() implies (value != null) }
            return value ?: raise(error = raise)
        }

        /**
         * Returns [check] cast to [IsType] if it is an instance of that type, otherwise
         * [raises][nz.adjmunro.outcome.raise.RaiseScope.raise] the [Error] produced by [error].
         *
         * @param Value The input value type.
         * @param IsType The expected type to cast to.
         * @param Error The error type.
         */
        public inline fun <Value, reified IsType: Any, Error> RaiseScope<Error>.ensureInstanceOf(
            check: Value,
            isType: KClass<IsType>,
            error: (Value) -> Error = { @Suppress(names = ["UNCHECKED_CAST"]) (it as Error) },
        ): IsType {
            contract { returns() implies (check is IsType) }
            if (check !is IsType) raise { error(check) }
            return check
        }

        /**
         * Suspend variant of [fold][nz.adjmunro.outcome.raise.RaiseScope.fold] that propagates
         * [CancellationException][kotlinx.coroutines.CancellationException] for structured concurrency.
         *
         * Executes [block] within this [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope]:
         * - On success, calls [close][nz.adjmunro.outcome.raise.RaiseScope.close] and applies [transform].
         * - On [shortCircuit][nz.adjmunro.outcome.raise.RaiseScope.shortCircuit], calls
         *   [close][nz.adjmunro.outcome.raise.RaiseScope.close] and applies [recover].
         * - On thrown [Exception], calls [ensureActive][kotlinx.coroutines.ensureActive] then
         *   [close][nz.adjmunro.outcome.raise.RaiseScope.close] and applies [catch].
         *
         * @param block The suspend block to execute within this scope.
         * @param catch Maps thrown [Exception]s to [Out]. Re-throws by default.
         * @param recover Maps a raised [Error] to [Out].
         * @param transform Maps a successful [In] value to [Out].
         * @return The [Out] result of [transform], [recover], or [catch].
         * @throws Throwable If [catch] or [recover] or [transform] throws.
         * @see nz.adjmunro.outcome.raise.RaiseScope.fold
         */
        @EnsuresActiveCoroutine
        public suspend inline fun <In, Out, Error> RaiseScope<Error>.folding(
            block: suspend (scope: RaiseScope<Error>) -> In,
            catch: (exception: Exception) -> Out = ::rethrow,
            recover: suspend (error: Error) -> Out,
            transform: suspend (value: In) -> Out,
        ): Out {
            @OptIn(UnsafeForCoroutineCancellation::class)
            return fold(
                block = { scope: RaiseScope<Error> -> block(scope) },
                catch = { e: Exception ->
                    if (e is CancellationException) {
                        currentCoroutineContext().ensureActive()
                    }

                    catch(e)
                },
                recover = { error: Error -> recover(error) },
                transform = { value: In -> transform(value) },
            )
        }

        /**
         * Executes [block] within this [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] and maps the result
         * using [transform], [recover], or [catch].
         *
         * - On success: calls [close][nz.adjmunro.outcome.raise.RaiseScope.close] and applies [transform].
         * - On [shortCircuit][nz.adjmunro.outcome.raise.RaiseScope.shortCircuit]: calls
         *   [close][nz.adjmunro.outcome.raise.RaiseScope.close] and applies [recover].
         * - On thrown [Exception]: calls [close][nz.adjmunro.outcome.raise.RaiseScope.close] and applies [catch].
         *
         * ***Warning:** [CancellationException][kotlinx.coroutines.CancellationException] is not propagated.
         * Use [folding][nz.adjmunro.outcome.raise.RaiseScope.folding] in suspend contexts.*
         *
         * @param block The block to execute within this scope.
         * @param catch Maps thrown [Exception]s to [Out]. Re-throws by default.
         * @param recover Maps a raised [Error] to [Out].
         * @param transform Maps a successful [In] value to [Out].
         * @return The [Out] result of [transform], [recover], or [catch].
         * @throws Throwable If [catch] or [recover] or [transform] throws.
         * @see nz.adjmunro.outcome.raise.RaiseScope.folding
         */
        @UnsafeForCoroutineCancellation
        public inline fun <In, Out, Error> RaiseScope<Error>.fold(
            block: (scope: RaiseScope<Error>) -> In,
            catch: (exception: Exception) -> Out = ::rethrow,
            recover: (error: Error) -> Out,
            transform: (value: In) -> Out,
        ): Out {
            contract {
                callsInPlace(lambda = block, kind = InvocationKind.AT_MOST_ONCE)
                callsInPlace(lambda = catch, kind = InvocationKind.AT_MOST_ONCE)
                callsInPlace(lambda = recover, kind = InvocationKind.AT_MOST_ONCE)
                callsInPlace(lambda = transform, kind = InvocationKind.AT_MOST_ONCE)
            }

            return try {
                val result: In = block(this@fold)
                close()
                transform(result)
            } catch (e: RaisedException) {
                close()
                @Suppress(names = ["UNCHECKED_CAST"])
                recover(e.error as Error)
            } catch (e: Exception) {
                close()
                catch(e) // Both block() and transform()
            }
        }

        /**
         * Type-hint helper — tells the compiler the [Error] type of this
         * [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] is [Nothing].
         * Use when type inference fails and no error can ever be raised.
         */
        public inline fun RaiseScope<Nothing>.nothing(): RaiseScope<Nothing> = this@nothing

        /**
         * Evaluates [error] and delegates to [shortCircuit][nz.adjmunro.outcome.raise.RaiseScope.shortCircuit],
         * immediately short-circuiting the [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] execution.
         *
         * This is the primary way to raise an error from within a scope block.
         *
         * @param error Produces the error value to raise.
         * @return [Nothing] — always throws.
         * @throws RaisedException [nz.adjmunro.outcome.raise.RaisedException] while the scope is active.
         * @throws RaiseScopeLeakedException [nz.adjmunro.outcome.raise.RaiseScopeLeakedException] if
         *   [close][nz.adjmunro.outcome.raise.RaiseScope.close] has already been called.
         */
        public inline fun <Error> RaiseScope<Error>.raise(
            error: () -> Error
        ): Nothing = shortCircuit(error = error())

        /**
         * Type-hint helper — tells the compiler the [Error] type of this
         * [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope].
         * Use when type inference fails and no value is available to infer from.
         *
         * @param Error The error type to assert.
         */
        public inline fun <Error> RaiseScope<Error>.raises(): RaiseScope<Error> = this@raises

        /**
         * Type-hint helper — tells the compiler the [Error] type of this
         * [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope] using a concrete [type] value.
         * Use when type inference fails and a representative value is available.
         *
         * @param Error The error type to assert.
         * @param type A representative value used only for type inference; never evaluated at runtime.
         */
        public inline fun <Error> RaiseScope<Error>.raises(type: Error): RaiseScope<Error> = this@raises

    }
}
