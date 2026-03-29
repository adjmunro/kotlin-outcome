package nz.adjmunro.outcome.raise

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic

/**
 * Default implementation of [RaiseScope][nz.adjmunro.outcome.raise.RaiseScope].
 *
 * Tracks scope lifetime with an atomic flag. While active,
 * [shortCircuit][nz.adjmunro.outcome.raise.RaiseScope.shortCircuit] throws
 * [RaisedException][nz.adjmunro.outcome.raise.RaisedException]. After
 * [close][nz.adjmunro.outcome.raise.RaiseScope.close] is called, it throws
 * [RaiseScopeLeakedException][nz.adjmunro.outcome.raise.RaiseScopeLeakedException] instead,
 * indicating the scope has been used beyond its intended lifetime.
 *
 * Prefer using a builder such as [outcomeOf][nz.adjmunro.outcome.members.outcomeOf] or
 * [faultOf][nz.adjmunro.outcome.members.faultOf] rather than constructing a
 * [DefaultRaiseScope][nz.adjmunro.outcome.raise.DefaultRaiseScope] directly. Construct one manually only
 * when you need to own the scope lifecycle yourself.
 *
 * @param Error The type of error that can be raised.
 * @see nz.adjmunro.outcome.raise.RaiseScope
 */
public open class DefaultRaiseScope<in Error>() : RaiseScope<Error> {
    private val active: AtomicBoolean = atomic(initial = true)

    override fun shortCircuit(error: Error): Nothing {
        when (active.value) {
            true -> throw RaisedException(error)
            false -> throw RaiseScopeLeakedException()
        }
    }

    override fun close() {
        active.getAndSet(value = false)
    }
}
