package nz.adjmunro.outcome.outcome

/**
 * Annotation marking members of the [Outcome] DSL.
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
internal annotation class OutcomeDsl
