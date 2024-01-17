package io.github.mmolosay.thecolor.presentation.home.input

import android.content.Context
import io.github.mmolosay.thecolor.presentation.home.R

fun ColorInputViewData(context: Context) =
    ColorInputUiData.ViewData(
        hexLabel = context.getString(R.string.color_input_tab_hex),
        rgbLabel = context.getString(R.string.color_input_tab_rgb),
    )