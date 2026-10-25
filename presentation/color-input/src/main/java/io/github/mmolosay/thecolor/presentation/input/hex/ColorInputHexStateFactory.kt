package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.model.causedByUser
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldDataFactory
import javax.inject.Inject

class ColorInputHexStateFactory @Inject constructor(
    private val textFieldDataFactory: TextFieldDataFactory,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
) {
    fun create(color: Color?): ColorInputHexState {
        val colorInput = run {
            val hex = with(colorConverter) { color?.toHex() }
            with(colorInputMapper) { hex?.toColorInput() }
        }
        val textField = textFieldDataFactory.create(
            text = TextFieldData.Text(colorInput?.string.orEmpty()) causedByUser false,
            isClearTextFeatureEnabled = true,
        )
        return ColorInputHexState(
            textField = textField,
            inputSubmissionResult = null,
        )
    }
}