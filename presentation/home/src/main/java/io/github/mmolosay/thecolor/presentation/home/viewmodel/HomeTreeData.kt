package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.utils.Lens

data class HomeTreeData(
    val home: HomeData,
    val colorPreview: ColorPreviewData,
    val colorCenter: ColorCenterTreeData?,
)

data class ColorCenterTreeData(
    // TODO: do later
//    val colorCenter: ColorCenterData,
//    val colorDetails: ColorDetailsState,
//    val colorScheme: ColorSchemeState,
    val selectedSwatchDetails: ColorDetailsState,
)

internal object HomeTreeDataLenses {

    val home by lazy {
        Lens<HomeTreeData, HomeData>(
            get = { s -> s.home },
            set = { s, v -> s.copy(home = v) },
        )
    }

    val colorPreview by lazy {
        Lens<HomeTreeData, ColorPreviewData>(
            get = { s -> s.colorPreview },
            set = { s, v -> s.copy(colorPreview = v) },
        )
    }

    // TODO: I don't like return of Idle object when the 'colorCenter' is null
    val selectedSwatchDetails = Lens<HomeTreeData, ColorDetailsState>(
        get = { t -> t.colorCenter?.selectedSwatchDetails ?: ColorDetailsState.Idle },
        set = { t, v -> t.mapColorCenter { it.copy(selectedSwatchDetails = v) } },
    )
}

internal fun HomeTreeData.mapColorCenter(
    transform: (ColorCenterTreeData) -> ColorCenterTreeData,
): HomeTreeData {
    val colorCenter = this.colorCenter ?: return this // node is gone, drop the write
    return copy(colorCenter = transform(colorCenter))
}