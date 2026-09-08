package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.center.ColorCenterHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.utils.Lens

data class HomeState(
    val tree: HomeTreeData,
    val colorCenterHandles: ColorCenterHandles?,
)

data class ColorCenterHandles(
    // TODO: ColorCenterHandle contains child Handles, but ColorCenterData doesn't contain child data. Why? Refactor?
    val colorCenter: ColorCenterHandle,
    val selectedSwatchDetails: ColorDetailsHandle,
)

object HomeStateLenses {

    val tree = Lens<HomeState, HomeTreeData>(
        get = { s -> s.tree },
        set = { s, v -> s.copy(tree = v) },
    )
}