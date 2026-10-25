package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorDetails

/**
 * Describes the current state (color-wise) of the 'Color Details'.
 */
class ColorDetailsSession(
    val seed: Color,
    val exact: Color,
) {
    companion object {
        fun fromSeedDetails(seedDetails: ColorDetails) =
            ColorDetailsSession(
                seed = seedDetails.color,
                exact = seedDetails.exact.color,
            )
    }
}

fun ColorDetailsSession.getByRole(role: ColorRole): Color =
    when (role) {
        ColorRole.Seed -> this.seed
        ColorRole.Exact -> this.exact
    }