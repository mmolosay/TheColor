package io.github.mmolosay.thecolor.input.hex

import android.content.Context
import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.R

data class ColorInputHexUiData(
    val textField: TextFieldUiData,
)

fun ColorInputHexViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_hex_label),
        placeholder = context.getString(R.string.color_input_hex_placeholder),
        prefix = context.getString(io.github.mmolosay.thecolor.presentation.R.string.color_hex_numbersign),
        trailingIcon = TextFieldUiData.ViewData.TrailingIcon.Exists(
            contentDesc = context.getString(R.string.color_input_hex_trailing_icon_desc),
        ),
    )

fun ColorInputHexUiData.assembleColorInput() =
    ColorInput.Hex(string = textField.text.string)