package io.github.mmolosay.thecolor.input.rgb

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.R
import io.github.mmolosay.thecolor.input.field.TextFieldUiData
import io.github.mmolosay.thecolor.input.field.TextFieldUiData.ViewData.TrailingIcon

data class ColorInputRgbViewData(
    val rInputField: TextFieldUiData.ViewData,
    val gInputField: TextFieldUiData.ViewData,
    val bInputField: TextFieldUiData.ViewData,
)

fun ColorInputRgbViewData(context: Context) =
    ColorInputRgbViewData(
        rInputField = rViewData(context),
        gInputField = gViewData(context),
        bInputField = bViewData(context),
    )

private fun rViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_r_label),
        placeholder = context.getString(R.string.color_input_rgb_r_placeholder),
        prefix = null,
        trailingIcon = TrailingIcon.None,
    )

private fun gViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_g_label),
        placeholder = context.getString(R.string.color_input_rgb_g_placeholder),
        prefix = null,
        trailingIcon = TrailingIcon.None,
    )

private fun bViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_b_label),
        placeholder = context.getString(R.string.color_input_rgb_b_placeholder),
        prefix = null,
        trailingIcon = TrailingIcon.None,
    )
