package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color

/**
 * Platform-agnostic data about an error that occurred in 'Color Details' feature.
 */
data class ColorDetailsError(
    val cause: Throwable,
    val origin: Origin,
) {

    /** An operation that produced a [ColorDetailsError]. */
    sealed interface Origin {
        data class SetSeedColor(val color: Color) : Origin
        data class SelectColor(val role: ColorRole) : Origin
    }
}