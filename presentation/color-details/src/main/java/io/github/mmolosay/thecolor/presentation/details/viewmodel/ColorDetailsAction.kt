package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.presentation.common.ExecuteAction

sealed interface ColorDetailsAction {

    data class SelectColor(
        val role: ColorRole,
    ) : ColorDetailsAction

    data object RetryOnError : ColorDetailsAction
}

typealias ExecuteColorDetailsAction = ExecuteAction<ColorDetailsAction>