package io.github.mmolosay.thecolor.utils

sealed interface Maybe<out T> {
    data object None : Maybe<Nothing>
    data class Some<T>(val value: T) : Maybe<T>
}

fun <T> Maybe<T>.requireValue(): T {
    require(this is Maybe.Some) { "Required to have value present, but there is none" }
    return this.value
}