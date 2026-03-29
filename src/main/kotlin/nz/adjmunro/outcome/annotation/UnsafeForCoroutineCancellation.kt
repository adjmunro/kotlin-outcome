package nz.adjmunro.outcome.annotation

/**
 * Opt-in marker for functions that **do not** call [ensureActive][kotlinx.coroutines.ensureActive],
 * meaning [CancellationException][kotlinx.coroutines.CancellationException] may be silently suppressed.
 *
 * Using these functions inside a `suspend` context can interfere with structured concurrency — a
 * cancelled coroutine may continue executing instead of terminating. Prefer the
 * [EnsuresActiveCoroutine][nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine]-annotated counterparts
 * in `suspend` contexts.
 *
 * **Supporting functions:**
 * [nullable][nz.adjmunro.outcome.nullable.nullable],
 * [result][nz.adjmunro.outcome.result.result],
 * [outcome][nz.adjmunro.outcome.members.outcome],
 * [maybe][nz.adjmunro.outcome.members.maybe],
 * [fault][nz.adjmunro.outcome.members.fault],
 * [catchString][nz.adjmunro.outcome.members.catchString],
 * [catchException][nz.adjmunro.outcome.members.catchException],
 * [RaiseScope.catch][nz.adjmunro.outcome.raise.RaiseScope.catching],
 * [RaiseScope.fold][nz.adjmunro.outcome.raise.RaiseScope.fold].
 *
 * @see nz.adjmunro.outcome.annotation.EnsuresActiveCoroutine
 */
@RequiresOptIn
@Target(allowedTargets = [AnnotationTarget.FUNCTION])
@Retention(value = AnnotationRetention.BINARY)
public annotation class UnsafeForCoroutineCancellation
