package nz.adjmunro.outcome.raise.exception

import nz.adjmunro.outcome.raise.RaiseDsl

@RaiseDsl
internal class RaiseScopeLeakedException : IllegalStateException("RaiseScope was leaked!")
