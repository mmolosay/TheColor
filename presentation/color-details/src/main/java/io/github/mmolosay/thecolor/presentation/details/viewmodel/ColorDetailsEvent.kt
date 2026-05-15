package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.domain.color.Color

/** An event that originates in 'Color Details' feature and is broadcast to outside. */
sealed interface ColorDetailsEvent {

    /** A [color] has been selected in 'Color Details' feature. */
    data class ColorSelected(
        val color: Color,
        val colorRole: ColorRole,
    ) : ColorDetailsEvent
}