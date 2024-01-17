package io.github.mmolosay.thecolor.presentation.home.input.rgb

import android.content.Context
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData

data class ColorInputRgbViewData(
    val rInputField: ColorInputFieldUiData.ViewData,
    val gInputField: ColorInputFieldUiData.ViewData,
    val bInputField: ColorInputFieldUiData.ViewData,
)

fun ColorInputRgbViewData(context: Context) =
    ColorInputRgbViewData(
        rInputField = rViewData(context),
        gInputField = gViewData(context),
        bInputField = bViewData(context),
    )

private fun rViewData(context: Context) =
    ColorInputFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_r_label),
        placeholder = context.getString(R.string.color_input_rgb_r_placeholder),
        prefix = null,
        trailingIconContentDesc = null,
    )

private fun gViewData(context: Context) =
    ColorInputFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_g_label),
        placeholder = context.getString(R.string.color_input_rgb_g_placeholder),
        prefix = null,
        trailingIconContentDesc = null,
    )

private fun bViewData(context: Context) =
    ColorInputFieldUiData.ViewData(
        label = context.getString(R.string.color_input_rgb_b_label),
        placeholder = context.getString(R.string.color_input_rgb_b_placeholder),
        prefix = null,
        trailingIconContentDesc = null,
    )
