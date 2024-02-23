package io.github.mmolosay.thecolor.presentation.preview

/**
 * Platform-agnostic data provided by ViewModel to color preview View.
 */
data class ColorPreviewData(
    val color: ColorInt,
) {

    /**
     * Solid color in RRGGBB format without alpha channel.
     */
    // TODO: extract all ColorInt-s across the project into reusable component in ':presentation:common' module
    @JvmInline
    value class ColorInt(val hex: Int)
}