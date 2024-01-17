package io.github.mmolosay.thecolor.presentation.home.input

data class ColorInputUiData(
    val inputType: InputType,
    val onInputTypeSelect: (InputType) -> Unit,
    val hexLabel: String,
    val rgbLabel: String,
) {

    enum class InputType {
        Hex,
        Rgb,
    }

    data class ViewData(
        val hexLabel: String,
        val rgbLabel: String,
    )
}