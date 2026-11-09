package io.github.mmolosay.thecolor.presentation.center.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

data class ColorCenterState(
    val details: ColorDetailsState,
    val scheme: ColorSchemeState,
)

object ColorCenterStateLenses {

    val details = Lens<ColorCenterState, ColorDetailsState>(
        get = { s -> s.details },
        set = { s, v -> s.copy(details = v) },
    )
    val scheme = Lens<ColorCenterState, ColorSchemeState>(
        get = { s -> s.scheme },
        set = { s, v -> s.copy(scheme = v) },
    )
}

sealed interface ColorCenterOutcome {

    data class SessionColorSelected(
        val color: Color,
    ) : ColorCenterOutcome

    data class SwatchSelected(
        val details: DomainColorDetails,
    ) : ColorCenterOutcome
}
