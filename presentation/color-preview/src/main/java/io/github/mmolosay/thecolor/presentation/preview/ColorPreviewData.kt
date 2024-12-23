package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.api.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to 'Color Preview' View.
 */
data class ColorPreviewData(
    val color: ColorInt?,
)