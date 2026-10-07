package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.withText

data class ColorInputRgbState(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
    val inputSubmissionResult: ColorInputSubmissionResult?,
    val isSmartBackspaceEnabled: Boolean,
)

fun ColorInputRgbState.colorInput(): ColorInput.Rgb =
    ColorInput.Rgb(
        r = this.rTextField.text.data.string,
        g = this.gTextField.text.data.string,
        b = this.bTextField.text.data.string,
    )

context(validator: ColorInputValidator)
fun ColorInputRgbState.color(): Color? {
    val colorInput = this.colorInput()
    return with(validator) { colorInput.validate() }.getColorOrNull()
}

fun ColorInputRgbState.toData(): ColorInputRgbData =
    ColorInputRgbData(
        rTextField = this.rTextField,
        gTextField = this.gTextField,
        bTextField = this.bTextField,
        inputSubmissionResult = this.inputSubmissionResult,
        isSmartBackspaceEnabled = this.isSmartBackspaceEnabled,
    )

context(
    converter: ColorConverter,
    inputMapper: ColorInputMapper,
)
fun ColorInputRgbState.withColor(color: Color?): ColorInputRgbState {
    val rgbColor = with(converter) { color?.toRgb() }
    val colorInput = if (rgbColor != null) {
        with(inputMapper) { rgbColor.toColorInput() }
    } else {
        ColorInput.Rgb(r = "", g = "", b = "")
    }
    fun String.toTextWithSource() =
        TextFieldData.Text(this) causedByUser false
    return this.copy(
        rTextField = this.rTextField.withText(colorInput.r.toTextWithSource()),
        gTextField = this.gTextField.withText(colorInput.g.toTextWithSource()),
        bTextField = this.bTextField.withText(colorInput.b.toTextWithSource()),
    )
}