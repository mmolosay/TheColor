package io.github.mmolosay.thecolor.presentation.home.viewmodel

import kotlinx.coroutines.Job

sealed interface HomeAction {

    data object Proceed : HomeAction

    data object RandomizeColor : HomeAction

    data object RequestToGoToSettings : HomeAction

    data object ClearProceedResult : HomeAction

    data class OnSideEffectProcessed(
        val se: HomeData.SideEffect,
    ) : HomeAction

    data object ClearColorSchemeSelectedSwatch : HomeAction
}

internal typealias ExecuteHomeAction = (HomeAction) -> Job