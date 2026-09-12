package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorDetails

/** An event that originates in 'Color Scheme' feature and is broadcast to outside. */
sealed interface ColorSchemeEvent {

    /** A [swatch] has been selected in 'Color Scheme' feature. */
    data class SelectSwatchAction(
        val swatch: ColorSchemeData.Swatch,
        val swatchColorDetails: ColorDetails,
    ) : ColorSchemeEvent

    data class ApplyChangesAction(
        val seed: Color,
    ) : ColorSchemeEvent

    data class RetryOnErrorAction(
        val seed: Color,
    ) : ColorSchemeEvent
}