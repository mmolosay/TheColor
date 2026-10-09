package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color

/**
 * Platform-agnostic data provided by ViewModel to 'HSV Color Input' View.
 */
data class ColorInputHsvData(
    val color: Color.Hsv?,
)