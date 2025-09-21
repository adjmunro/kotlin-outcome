package nz.adjmunro.knomadic.raise.exception

import nz.adjmunro.knomadic.raise.RaiseDsl

@RaiseDsl
internal class RaiseScopeLeakedException : IllegalStateException("RaiseScope was leaked!")
