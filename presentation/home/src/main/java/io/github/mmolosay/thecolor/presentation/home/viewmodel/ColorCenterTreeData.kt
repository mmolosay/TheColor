package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState
import io.github.mmolosay.thecolor.utils.Lens

data class ColorCenterTreeData(
    val colorCenter: ColorCenterData,
    val colorDetails: ColorDetailsState,
    val colorScheme: ColorSchemeState,
    val selectedSwatchDetails: ColorDetailsState,
)

internal object ColorCenterTreeDataLenses {

    val colorCenter = Lens<ColorCenterTreeData, ColorCenterData>(
        get = { s -> s.colorCenter },
        set = { s, v -> s.copy(colorCenter = v) },
    )

    val colorDetails = Lens<ColorCenterTreeData, ColorDetailsState>(
        get = { s -> s.colorDetails },
        set = { s, v -> s.copy(colorDetails = v) },
    )

    val colorScheme = Lens<ColorCenterTreeData, ColorSchemeState>(
        get = { s -> s.colorScheme },
        set = { s, v -> s.copy(colorScheme = v) },
    )

    val selectedSwatchDetails = Lens<ColorCenterTreeData, ColorDetailsState>(
        get = { s -> s.selectedSwatchDetails },
        set = { s, v -> s.copy(selectedSwatchDetails = v) },
    )
}