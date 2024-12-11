package nz.adjmunro.nomadic.error.fetch

sealed interface Fetch<out T : Any> {

    data object NotStarted : Fetch<Nothing>

    data object InProgress : Fetch<Nothing>

    @JvmInline
    value class Completed<out T : Any>(val result: T) : Fetch<T>

}
