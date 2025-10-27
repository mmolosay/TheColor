package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.impl.colorint.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to 'Color Preview' View.
 */
data class ColorPreviewData(
    val color: ColorInt?,
)

val ColorPreviewData.hasColor: Boolean
    get() = (this.color != null)

val ColorPreviewData.hasNoColor: Boolean
    get() = !this.hasColor

fun ColorPreviewData.requireColor() =
    requireNotNull(this.color)