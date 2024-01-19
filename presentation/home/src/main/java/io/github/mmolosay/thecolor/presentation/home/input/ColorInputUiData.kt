package io.github.mmolosay.thecolor.presentation.home.input

data class ColorInputUiData(
    val viewType: ViewType,
    val onInputTypeSelect: (ViewType) -> Unit,
    val hexLabel: String,
    val rgbLabel: String,
) {

    /** A type of color input View.*/
    enum class ViewType {
        Hex,
        Rgb,
    }

    data class ViewData(
        val hexLabel: String,
        val rgbLabel: String,
    )
}