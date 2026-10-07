package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.withText
import io.github.mmolosay.thecolor.utils.Lens

data class ColorInputHexState(
    val textField: TextFieldData,
    val inputSubmissionResult: ColorInputSubmissionResult?,
    val color: Color?,
)

object ColorInputHexStateLenses {
    val textField = Lens<ColorInputHexState, TextFieldData>(
        get = { s -> s.textField },
        set = { s, v -> s.copy(textField = v) },
    )
}

fun ColorInputHexState.toData(): ColorInputHexData =
    ColorInputHexData(
        textField = this.textField,
        inputSubmissionResult = this.inputSubmissionResult,
    )

context(
    converter: ColorConverter,
    inputMapper: ColorInputMapper,
)
fun ColorInputHexState.withColor(color: Color?): ColorInputHexState {
    val colorInput = if (color != null) {
        val hexColor = with(converter) { color.toHex() }
        with(inputMapper) { hexColor.toColorInput() }
    } else {
        ColorInput.Hex(string = "")
    }
    val textWithSource = TextFieldData.Text(colorInput.string) causedByUser false
    return this.copy(
        textField = this.textField.withText(textWithSource),
        color = color,
    )
}