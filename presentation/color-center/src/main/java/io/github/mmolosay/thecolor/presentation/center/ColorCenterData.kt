package io.github.mmolosay.thecolor.presentation.center

import kotlinx.collections.immutable.ImmutableList
import io.github.mmolosay.thecolor.utils.SideEffect as UtilsSideEffect

/**
 * Platform-agnostic data provided by ViewModel to 'Color Center' View.
 */
data class ColorCenterData(
    val sideEffects: ImmutableList<SideEffect>,
) {

    sealed interface SideEffect : UtilsSideEffect {

        data class ChangePage(
            override val id: UtilsSideEffect.Id,
            val pageIndex: Int,
        ) : SideEffect
    }
}