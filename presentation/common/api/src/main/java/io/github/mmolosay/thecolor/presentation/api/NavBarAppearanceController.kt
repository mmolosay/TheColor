package io.github.mmolosay.thecolor.presentation.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller of navigation bar's appearance.
 * It supports "layering" appearances, so that if the latest appearance is cancelled,
 * then the one that was before it will be used (as when going back to previous screen).
 */
class NavBarAppearanceController : NavBarAppearanceStack {

    private val appearanceStack = mutableListOf<NavBarAppearance>()

    private val _appearanceFlow = MutableStateFlow<NavBarAppearance?>(null)
    val appearanceFlow = _appearanceFlow.asStateFlow()

    override fun push(appearance: NavBarAppearance) {
        appearance.addToStackAndEmitFromFlow()
    }

    override fun peel() {
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
 * An interface to share with UI with only functionality that UI needs.
 * Interface Segregation Principle.
 *
 * @see NavBarAppearanceController
 */
interface NavBarAppearanceStack {
    fun push(appearance: NavBarAppearance)
    fun peel()
}

/**
 * A "no-operation" implementation of [NavBarAppearanceStack].
 * Useful in Compose Previews.
 */
object NoopNavBarAppearanceStack : NavBarAppearanceStack {
    override fun push(appearance: NavBarAppearance) {}
    override fun peel() {}
}

/**
 * Platform-agnostic model of navigation bar's appearance.
 *
 * @param color a color integer in `ARGB` format.
 */
data class NavBarAppearance(
    val color: Int,
    val isLight: Boolean,
)