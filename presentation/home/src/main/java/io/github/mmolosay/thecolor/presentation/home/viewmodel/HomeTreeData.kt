package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeState
import io.github.mmolosay.thecolor.utils.Lens
import kotlinx.collections.immutable.persistentListOf

data class HomeTreeData(
    val home: HomeData,
    val colorPreview: ColorPreviewData,
    val colorCenter: ColorCenterTreeData?,
)

data class ColorCenterTreeData(
    val colorCenter: ColorCenterData,
    val colorDetails: ColorDetailsState,
    val colorScheme: ColorSchemeState,
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

    // TODO: I don't like return of "null object" when the 'colorCenter' is null
    val colorCenter = Lens<HomeTreeData, ColorCenterData>(
        get = { t ->
            t.colorCenter?.colorCenter ?: ColorCenterData(sideEffects = persistentListOf())
        },
        set = { t, v -> t.mapColorCenter { it.copy(colorCenter = v) } },
    )

    // TODO: I don't like return of "null object"object when the 'colorCenter' is null
    val colorDetails = Lens<HomeTreeData, ColorDetailsState>(
        get = { t -> t.colorCenter?.colorDetails ?: ColorDetailsState.Idle },
        set = { t, v -> t.mapColorCenter { it.copy(colorDetails = v) } },
    )

    // TODO: I don't like return of "null object" when the 'colorCenter' is null
    val colorScheme = Lens<HomeTreeData, ColorSchemeState>(
        get = { t -> t.colorCenter?.colorScheme ?: ColorSchemeState.Idle },
        set = { t, v -> t.mapColorCenter { it.copy(colorScheme = v) } },
    )

    // TODO: I don't like return of "null object" when the 'colorCenter' is null
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