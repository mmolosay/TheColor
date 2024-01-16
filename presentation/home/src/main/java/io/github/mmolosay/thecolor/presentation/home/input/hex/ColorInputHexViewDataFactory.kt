package io.github.mmolosay.thecolor.presentation.home.input.hex

import android.content.Context
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.R as CommonR

@Suppress("FunctionName")
fun ColorInputHexViewData(context: Context) =
    ColorInputFieldUiData.ViewData(
        label = context.getString(R.string.color_input_hex_label),
        placeholder = context.getString(R.string.color_input_hex_placeholder),
        prefix = context.getString(CommonR.string.color_hex_numbersign),
        trailingIconContentDesc = context.getString(R.string.color_input_hex_trailing_icon_desc),
    )