package io.github.mmolosay.thecolor.presentation.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller of navigation bar's appearance.
 * It supports "layering" appearances, so that if the latest appearance is cancelled,
 * then the one that was before it will be used (as when going back to previous screen).
 */
class NavBarAppearanceController {

    private val appearanceStack = mutableListOf<NavBarAppearance>()

    private val _appearanceFlow = MutableStateFlow<NavBarAppearance?>(null)
    val appearanceFlow = _appearanceFlow.asStateFlow()

    infix fun push(appearance: NavBarAppearance) {
        appearance.addToStackAndEmitFromFlow()
    }

    fun peel() {
        appearanceStack.removeLastOrNull()
        emitLatestAppearance()
    }

    private fun NavBarAppearance.addToStackAndEmitFromFlow() {
        appearanceStack += this
        emitLatestAppearance()
    }

    private fun emitLatestAppearance() {
        val latest = appearanceStack.lastOrNull()
        _appearanceFlow.value = latest
    }
}

/**
 * Platform-agnostic model of navigation bar's appearance.
 */
data class NavBarAppearance(
    val color: ColorInt,
    val isLight: Boolean,
)