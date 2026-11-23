package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData

data class HomeViewData(
    val canProceed: Boolean,
    val sessionColor: SessionColor?,
    val colorPreview: ColorPreviewData,
    val swatchSession: SwatchSession?,
) {

    data class SessionColor(
        val color: ColorInt,
        val isDark: Boolean,
    )
}