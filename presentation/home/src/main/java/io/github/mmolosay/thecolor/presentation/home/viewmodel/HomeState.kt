package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.utils.Lens

sealed interface HomeState {

    data object Initializing : HomeState

    data class Ready(
        val data: HomeData,
        val colorPreview: ColorPreviewData,
        val colorCenterViewModel: ColorCenterViewModel?,
        val selectedSwatchDetails: ColorDetailsState, // TODO: rework as 'Color Preview' is done
        val selectedSwatchDetailsHandle: ColorDetailsHandle?,
    ) : HomeState
}

internal fun HomeState.requireReady(): HomeState.Ready {
    require(this is HomeState.Ready)
    return this
}

object HomeStateLenses {

    val homeData by lazy {
        Lens<HomeState, HomeData>(
            get = { s -> s.requireReady().data },
            set = { s, v -> s.requireReady().copy(data = v) },
        )
    }

    val colorPreview by lazy {
        Lens<HomeState, ColorPreviewData>(
            get = { s -> s.requireReady().colorPreview },
            set = { s, v -> s.requireReady().copy(colorPreview = v) },
        )
    }

    val selectedSwatchDetails by lazy {
        Lens<HomeState, ColorDetailsState>(
            get = { s -> s.requireReady().selectedSwatchDetails },
            set = { s, v -> s.requireReady().copy(selectedSwatchDetails = v) },
        )
    }
}