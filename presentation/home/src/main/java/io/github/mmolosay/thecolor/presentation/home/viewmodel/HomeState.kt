package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData

data class HomeState(
    val home: HomeData,
    val colorPreview: ColorPreviewData,
    val colorCenterHandles: ColorCenterHandles?,
)

data class ColorCenterHandles(
    // TODO: ColorCenterHandle contains child Handles, but ColorCenterData doesn't contain child data. Why? Refactor?
    val colorCenter: ColorCenterHandle,
    val selectedSwatchDetails: ColorDetailsHandle,
)