package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.utils.Lens

sealed interface HomeState {

    data object Initializing : HomeState

    data class Ready(
        val tree: HomeTreeData,
        val colorCenterViewModel: ColorCenterViewModel?,
        val selectedSwatchDetailsHandle: ColorDetailsHandle?,
    ) : HomeState
}

internal fun HomeState.requireReady(): HomeState.Ready {
    require(this is HomeState.Ready)
    return this
}

object HomeStateLenses {

    val tree by lazy {
        Lens<HomeState, HomeTreeData>(
            get = { s -> s.requireReady().tree },
            set = { s, v -> s.requireReady().copy(tree = v) },
        )
    }
}