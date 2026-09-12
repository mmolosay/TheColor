package io.github.mmolosay.thecolor.presentation.center

import kotlinx.coroutines.Job

sealed interface ColorCenterAction {

    data class ChangePage(
        val pageIndex: Int,
    ) : ColorCenterAction

    data class OnSideEffectProcessed(
        val se: ColorCenterData.SideEffect,
    ) : ColorCenterAction
}

typealias ExecuteColorCenterAction = (ColorCenterAction) -> Job