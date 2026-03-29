package nz.adjmunro.outcome.annotation

/**
 * Marks `suspend` functions that [ensureActive][kotlinx.coroutines.ensureActive].
 *
 * [CancellationException][kotlinx.coroutines.CancellationException] is the signal used by Kotlin's structured
 * concurrency model to cancel a coroutine, and ***must be rethrown when the coroutine is no longer active.***
 * Because `CancellationException` has subclasses that represent real domain exceptions, a simple `is`-check
 * is not sufficient — [ensureActive][kotlinx.coroutines.ensureActive] is required.
 *
 * Functions bearing this annotation implement the pattern below:
 * ```kotlin
 * try { /* ... */ } catch (e: Exception) {
 *     if (e is CancellationException) currentCoroutineContext().ensureActive()
 *     // safe to handle or swallow e here
 * }
 * ```
 *
 * **Supporting functions:**
 * [nullableOf][nz.adjmunro.outcome.nullable.nullableOf],
 * [resultOf][nz.adjmunro.outcome.result.resultOf],
 * [outcomeOf][nz.adjmunro.outcome.members.outcomeOf],
 * [maybeOf][nz.adjmunro.outcome.members.maybeOf],
 * [faultOf][nz.adjmunro.outcome.members.faultOf],
 * [catchStringOf][nz.adjmunro.outcome.members.catchStringOf],
 * [catchExceptionOf][nz.adjmunro.outcome.members.catchExceptionOf],
 * [RaiseScope.catching][nz.adjmunro.outcome.raise.RaiseScope.catching],
 * [RaiseScope.folding][nz.adjmunro.outcome.raise.RaiseScope.folding].
 *
 * @see nz.adjmunro.outcome.annotation.UnsafeForCoroutineCancellation
 */
@DslMarker
@Target(allowedTargets = [AnnotationTarget.FUNCTION])
@Retention(value = AnnotationRetention.SOURCE)
public annotation class EnsuresActiveCoroutine
