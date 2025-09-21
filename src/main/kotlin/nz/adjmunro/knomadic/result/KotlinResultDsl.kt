package nz.adjmunro.knomadic.result

import nz.adjmunro.knomadic.outcome.Outcome

/**
 * Annotation marking members of the [KotlinResult] DSL.
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
internal annotation class KotlinResultDsl
