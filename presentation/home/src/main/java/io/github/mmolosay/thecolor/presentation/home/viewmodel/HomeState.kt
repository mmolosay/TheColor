package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.utils.Lens

sealed interface HomeState {

    data object Initializing : HomeState

    data class Ready(
        val tree: HomeTreeData,
        val colorCenterHandles: ColorCenterHandles?,
    ) : HomeState
}

data class ColorCenterHandles(
    // TODO: ColorCenterHandle contains child Handles, but ColorCenterData doesn't contain child data. Why? Refactor?
    val colorCenter: ColorCenterHandle,
    val selectedSwatchDetails: ColorDetailsHandle,
)

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