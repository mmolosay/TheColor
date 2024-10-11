package io.github.mmolosay.thecolor.presentation.input.impl.hex

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.impl.R
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiStrings

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorInputHexUiStrings(
    val textField: TextFieldUiStrings,
)

fun ColorInputHexUiStrings(context: Context) =
    ColorInputHexUiStrings(
        textField = TextFieldUiStrings(context),
    )

private fun TextFieldUiStrings(context: Context) =
    TextFieldUiStrings(
        label = context.getString(R.string.color_input_hex_label),
        placeholder = context.getString(R.string.color_input_hex_placeholder),
        prefix = "#",
        trailingIcon = TextFieldUiStrings.TrailingIcon.Exists(
            contentDesc = context.getString(R.string.color_input_hex_trailing_icon_desc),
        ),
    )