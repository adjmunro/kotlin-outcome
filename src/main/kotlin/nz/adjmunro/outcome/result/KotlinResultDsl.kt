package nz.adjmunro.outcome.result

import nz.adjmunro.outcome.outcome.Outcome

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
