package io.github.mmolosay.thecolor.presentation.input.impl

/**
 * Platform-agnostic data provided by ViewModel to 'Color Input' View.
 */
data class ColorInputData(
    val selectedViewType: ViewType,
    val orderedViewTypes: List<ViewType>,
    val onInputTypeChange: (ViewType) -> Unit,
) {
    /** A type of 'Color Input' View.*/
    enum class ViewType {
        Hex,
        Rgb,
    }
}