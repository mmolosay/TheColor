package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to color preview View.
 */
data class ColorPreviewData(
    val color: ColorInt,
)