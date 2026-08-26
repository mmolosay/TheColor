package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme
import io.github.mmolosay.thecolor.domain.color.ColorScheme.Mode as DomainMode

sealed interface ColorSchemeState {

    data object Idle : ColorSchemeState

    data class Loading(
        val request: Request,
    ) : ColorSchemeState

    data class Ready(
        val request: Request,
        val data: ColorSchemeData,
        val domainColorScheme: DomainColorScheme,
    ) : ColorSchemeState

    data class Error(
        val request: Request,
        val error: ColorSchemeError,
    ) : ColorSchemeState

    data class Request(
        val seed: Color,
        val mode: DomainMode,
        val swatchCount: SwatchCount,
    )
}