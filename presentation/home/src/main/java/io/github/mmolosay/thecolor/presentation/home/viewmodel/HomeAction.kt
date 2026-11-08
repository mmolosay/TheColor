package io.github.mmolosay.thecolor.presentation.home.viewmodel

import io.github.mmolosay.thecolor.presentation.common.ExecuteAction

sealed interface HomeAction {

    data object Proceed : HomeAction

    data object RandomizeColor : HomeAction

    data object RequestToGoToSettings : HomeAction

    data class OnProceedResultProcessed(
        val result: HomeData.ProceedResult,
    ) : HomeAction

    data class OnSideEffectProcessed(
        val se: HomeData.SideEffect,
    ) : HomeAction

    data object ClearColorSchemeSelectedSwatch : HomeAction
}

typealias ExecuteHomeAction = ExecuteAction<HomeAction>