package io.github.mmolosay.thecolor.presentation.input.impl.rgb

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.impl.R
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData

/**
 * Framework-oriented data required for RGB color input View to be presented by Compose.
 */
data class ColorInputRgbUiData(
    val rTextField: TextFieldUiData,
    val gTextField: TextFieldUiData,
    val bTextField: TextFieldUiData,
    val onImeActionDone: () -> Unit,
) {

    /**
     * Part of to-be [ColorInputRgbUiData].
     * Framework-oriented.
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val rTextField: TextFieldUiData.ViewData,
        val gTextField: TextFieldUiData.ViewData,
        val bTextField: TextFieldUiData.ViewData,
    )
}

fun ColorInputRgbViewData(context: Context) =
    ColorInputRgbUiData.ViewData(
        rTextField = TextFieldRViewData(context),
        gTextField = TextFieldGViewData(context),
        bTextField = TextFieldBViewData(context),
    )

private fun TextFieldRViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_r_label),
        placeholder = context.getString(R.string.color_input_rgb_r_placeholder),
        prefix = null,
        trailingIcon = TextFieldUiData.ViewData.TrailingIcon.None,
    )

private fun TextFieldGViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_g_label),
        placeholder = context.getString(R.string.color_input_rgb_g_placeholder),
        prefix = null,
        trailingIcon = TextFieldUiData.ViewData.TrailingIcon.None,
    )

private fun TextFieldBViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_b_label),
        placeholder = context.getString(R.string.color_input_rgb_b_placeholder),
        prefix = null,
        trailingIcon = TextFieldUiData.ViewData.TrailingIcon.None,
    )