package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.color.ColorDetails as DomainColorDetails

/** An event that originates in 'Color Scheme' feature and is broadcast to outside. */
sealed interface ColorSchemeEvent {

    /** A [swatch] has been selected in 'Color Scheme' feature. */
    data class SwatchSelected(
        val swatch: ColorSchemeData.Swatch,
        val swatchColorDetails: DomainColorDetails,
    ) : ColorSchemeEvent
}