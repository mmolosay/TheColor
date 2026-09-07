package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.utils.Lens

data class HomeTreeData(
    val home: HomeData,
    val colorPreview: ColorPreviewData,
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
}