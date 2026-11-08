package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.utils.Identity
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

sealed interface ColorSchemeState {

    data object NotRequested : ColorSchemeState

    data class Loading(
        val request: Request,
    ) : ColorSchemeState

    data class Ready(
        val scheme: DomainColorScheme,
        val selectedMode: DomainColorScheme.Mode,
        val selectedSwatchCount: SwatchCount,
        val request: Request,
    ) : ColorSchemeState

    data class Error(
        val cause: Throwable,
        val request: Request,
    ) : ColorSchemeState

    data class Request(
        val seed: Color,
        val mode: DomainColorScheme.Mode,
        val swatchCount: SwatchCount,
        val identity: Identity = Identity(),
    )
}

enum class SwatchCount(val value: Int) {
    Three(3),
    Four(4),
    Six(6),
    Nine(9),
    Thirteen(13),
    Eighteen(18),
}

sealed interface ColorSchemeOutcome {

    data class SwatchSelected(
        val details: DomainColorDetails,
    ) : ColorSchemeOutcome
}