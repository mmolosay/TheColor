package io.github.mmolosay.thecolor.presentation.details.viewmodel

import kotlinx.coroutines.Job

sealed interface ColorDetailsAction {

    data class SelectColor(
        val role: ColorRole,
    ) : ColorDetailsAction

    data object RetryOnError : ColorDetailsAction
}

typealias ExecuteColorDetailsAction = (ColorDetailsAction) -> Job