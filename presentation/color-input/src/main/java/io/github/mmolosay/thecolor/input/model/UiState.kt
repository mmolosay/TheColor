package io.github.mmolosay.thecolor.input.model

sealed interface UiState<out T> {
    data object BeingInitialized : UiState<Nothing>
    data class Ready<T>(val uiData: T) : UiState<T>
}

fun <UiData> UiData?.toUiSate(): UiState<UiData> =
    if (this == null) UiState.BeingInitialized
    else UiState.Ready(uiData = this)