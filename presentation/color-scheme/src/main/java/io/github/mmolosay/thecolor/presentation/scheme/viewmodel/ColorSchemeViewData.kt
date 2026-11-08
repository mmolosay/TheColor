package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

sealed interface ColorSchemeViewData {

    data object Empty : ColorSchemeViewData

    data object Loading : ColorSchemeViewData

    data class Ready(
        val data: ColorSchemeData,
    ) : ColorSchemeViewData

    data class Error(
        val cause: Throwable,
    ) : ColorSchemeViewData
}