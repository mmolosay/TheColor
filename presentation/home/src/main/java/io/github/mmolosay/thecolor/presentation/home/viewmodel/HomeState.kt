package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeHandle
import io.github.mmolosay.thecolor.utils.Lens

data class HomeState(
    val home: HomeData,
    val colorPreview: ColorPreviewData,
    val colorCenterHandles: ColorCenterHandles?,
)

data class ColorCenterHandles(
    val colorCenter: ColorCenterHandle,
    val colorDetails: ColorDetailsHandle,
    val colorScheme: ColorSchemeHandle,
    val selectedSwatchDetails: ColorDetailsHandle,
)

internal object HomeStateLenses {

    val home = Lens<HomeState, HomeData>(
        get = { s -> s.home },
        set = { s, v -> s.copy(home = v) },
    )
}