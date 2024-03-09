package io.github.mmolosay.thecolor.presentation.input.hex

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.R
import io.github.mmolosay.thecolor.presentation.input.field.TextFieldUiData

/**
 * Framework-oriented data required for HEX color input View to be presented by Compose.
 */
data class ColorInputHexUiData(
    val textField: TextFieldUiData,
) {

    /**
     * Part of to-be [ColorInputHexUiData].
     * Framework-oriented.
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val textField: TextFieldUiData.ViewData,
    )
}

fun ColorInputHexViewData(context: Context) =
    ColorInputHexUiData.ViewData(
        textField = TextFieldViewData(context),
    )

private fun TextFieldViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_hex_label),
        placeholder = context.getString(R.string.color_input_hex_placeholder),
        prefix = "#",
        trailingIcon = TextFieldUiData.ViewData.TrailingIcon.Exists(
            contentDesc = context.getString(R.string.color_input_hex_trailing_icon_desc),
        ),
    )