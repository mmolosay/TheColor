package io.github.mmolosay.thecolor.presentation.input.model

sealed interface DataState<out T> {
    data object BeingInitialized : DataState<Nothing>
    data class Ready<T>(val data: T) : DataState<T>
}

internal fun <T> T?.asDataState(): DataState<T> =
    when {
        this == null -> DataState.BeingInitialized
        else -> DataState.Ready(data = this)
    }