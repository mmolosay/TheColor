package io.github.mmolosay.thecolor.presentation.input.impl

import android.content.Context

/**
 * Strings that are pre-defined in UI and don't come from ViewModel.
 *
 * This object is created by View, since string resources are tied to platform-specific
 * components (like Context), which should be avoided in ViewModels.
 */
data class ColorInputUiStrings(
    val hexLabel: String,
    val rgbLabel: String,
)

fun ColorInputUiStrings(context: Context) =
    ColorInputUiStrings(
        hexLabel = context.getString(R.string.color_input_tab_hex),
        rgbLabel = context.getString(R.string.color_input_tab_rgb),
    )