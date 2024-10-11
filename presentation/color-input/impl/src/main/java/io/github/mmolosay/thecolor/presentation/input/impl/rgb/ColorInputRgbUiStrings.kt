package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.impl.R
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiStrings

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorInputRgbUiStrings(
    val rTextField: TextFieldUiStrings,
    val gTextField: TextFieldUiStrings,
    val bTextField: TextFieldUiStrings,
)

fun ColorInputRgbUiStrings(context: Context) =
    ColorInputRgbUiStrings(
        rTextField = TextFieldRUiStrings(context),
        gTextField = TextFieldGUiStrings(context),
        bTextField = TextFieldBUiStrings(context),
    )

private fun TextFieldRUiStrings(context: Context) =
    TextFieldUiStrings(
        label = context.getString(R.string.color_input_rgb_r_label),
        placeholder = context.getString(R.string.color_input_rgb_r_placeholder),
        prefix = null,
        trailingIcon = TextFieldUiStrings.TrailingIcon.None,
    )

private fun TextFieldGUiStrings(context: Context) =
    TextFieldUiStrings(
        label = context.getString(R.string.color_input_rgb_g_label),
        placeholder = context.getString(R.string.color_input_rgb_g_placeholder),
        prefix = null,
        trailingIcon = TextFieldUiStrings.TrailingIcon.None,
    )

private fun TextFieldBUiStrings(context: Context) =
    TextFieldUiStrings(
        label = context.getString(R.string.color_input_rgb_b_label),
        placeholder = context.getString(R.string.color_input_rgb_b_placeholder),
        prefix = null,
        trailingIcon = TextFieldUiStrings.TrailingIcon.None,
    )