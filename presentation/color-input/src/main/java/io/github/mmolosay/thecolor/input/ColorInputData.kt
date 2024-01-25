package io.github.mmolosay.thecolor.input

/**
 * Platform-agnostic data provided by ViewModel to color input View.
 */
data class ColorInputData(
    val viewType: ViewType,
    val onInputTypeChange: (ViewType) -> Unit,
) {
    /** A type of color input View.*/
    enum class ViewType {
        Hex,
        Rgb,
    }
}