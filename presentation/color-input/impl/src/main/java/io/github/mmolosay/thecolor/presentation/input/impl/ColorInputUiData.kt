package io.github.mmolosay.thecolor.presentation.input.impl

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputData.ViewType

/**
 * Framework-oriented data required for color input View to be presented by Compose.
 */
data class ColorInputUiData(
    val viewType: ViewType,
    val onInputTypeChange: (ViewType) -> Unit,
    val hexLabel: String,
    val rgbLabel: String,
) {

    /**
     * Part of to-be [ColorInputUiData].
     * Framework-oriented.
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val hexLabel: String,
        val rgbLabel: String,
    )
}

fun ColorInputViewData(context: Context) =
    ColorInputUiData.ViewData(
        hexLabel = context.getString(R.string.color_input_tab_hex),
        rgbLabel = context.getString(R.string.color_input_tab_rgb),
    )