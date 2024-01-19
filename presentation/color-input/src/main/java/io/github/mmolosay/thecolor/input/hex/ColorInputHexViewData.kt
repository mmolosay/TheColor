package io.github.mmolosay.thecolor.input.hex

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.R
import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.presentation.R as CommonR

fun ColorInputHexViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_hex_label),
        placeholder = context.getString(R.string.color_input_hex_placeholder),
        prefix = context.getString(CommonR.string.color_hex_numbersign),
        trailingIcon = TrailingIcon.Exists(
            contentDesc = context.getString(R.string.color_input_hex_trailing_icon_desc),
        ),
    )