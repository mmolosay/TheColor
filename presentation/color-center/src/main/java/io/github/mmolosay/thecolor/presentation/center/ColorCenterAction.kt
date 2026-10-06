package io.github.mmolosay.thecolor.presentation.center

import io.github.mmolosay.thecolor.presentation.common.ExecuteAction

sealed interface ColorCenterAction {

    data class ChangePage(
        val pageIndex: Int,
    ) : ColorCenterAction

    data class OnSideEffectProcessed(
        val se: ColorCenterData.SideEffect,
    ) : ColorCenterAction
}

typealias ExecuteColorCenterAction = ExecuteAction<ColorCenterAction>