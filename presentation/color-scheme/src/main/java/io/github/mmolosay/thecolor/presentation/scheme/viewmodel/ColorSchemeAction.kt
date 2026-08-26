package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import kotlinx.coroutines.Job
import io.github.mmolosay.thecolor.domain.color.ColorScheme.Mode as DomainMode

sealed interface ColorSchemeAction {

    data class OnSwatchSelect(
        val index: Int,
    ) : ColorSchemeAction

    data class SelectMode(
        val mode: DomainMode,
    ) : ColorSchemeAction

    data class SelectSwatchCount(
        val count: ColorSchemeData.SwatchCount,
    ) : ColorSchemeAction

    data object ApplyChanges : ColorSchemeAction

    data object RetryOnError : ColorSchemeAction
}

typealias ExecuteColorSchemeAction = (ColorSchemeAction) -> Job