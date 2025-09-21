package nz.adjmunro.knomadic.raise

/**
 * Annotation marking members of the [RaiseScope] DSL.
 */
@Target(
    AnnotationTarget.FILE,
    AnnotationTarget.TYPEALIAS,
    AnnotationTarget.CLASS,
    AnnotationTarget.CONSTRUCTOR,
    AnnotationTarget.FUNCTION,
    AnnotationTarget.PROPERTY,
)
@DslMarker @PublishedApi
internal annotation class RaiseDsl
