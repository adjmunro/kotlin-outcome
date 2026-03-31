package nz.adjmunro.outcome.raise

@PublishedApi
internal class RaiseScopeLeakedException : IllegalStateException("RaiseScope was leaked!")
