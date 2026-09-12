package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorScheme

sealed interface ColorSchemeState {

    data object Idle : ColorSchemeState

    data class Loading(
        val request: Request,
    ) : ColorSchemeState

    data class Ready(
        val request: Request,
        val data: ColorSchemeData,
        val domainColorScheme: ColorScheme,
    ) : ColorSchemeState

    data class Error(
        val request: Request,
        val error: ColorSchemeError,
    ) : ColorSchemeState

    data class Request(
        val seed: Color,
        val mode: ColorScheme.Mode,
        val swatchCount: ColorSchemeData.SwatchCount,
    )
}

internal fun ColorSchemeState.isAwaiting(request: ColorSchemeState.Request): Boolean =
    (this is ColorSchemeState.Loading) && (this.request == request)