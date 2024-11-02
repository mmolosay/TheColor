package io.github.mmolosay.thecolor.presentation.api

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Controller of navigation bar's appearance.
 * It supports "layering" appearances, so that if the latest appearance is cancelled,
 * then the one that was before it will be used (as when going back to previous screen).
 */
class NavBarAppearanceController : NavBarAppearanceStack {

    private val appearanceStack = mutableListOf<NavBarAppearance.WithTag>()

    private val _appearanceFlow = MutableStateFlow<NavBarAppearance.WithTag?>(null)
    val appearanceFlow = _appearanceFlow.asStateFlow()

    override fun push(appearance: NavBarAppearance.WithTag) {
        appearance.addToStackAndEmitFromFlow()
    }

    override fun peel() {
        appearanceStack.removeLastOrNull()
        emitLatestAppearance()
    }

    private fun NavBarAppearance.WithTag.addToStackAndEmitFromFlow() {
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
    fun push(appearance: NavBarAppearance.WithTag)
    fun peel()
}

fun NavBarAppearanceStack.push(appearance: NavBarAppearance) {
    val tagged = NavBarAppearance.WithTag(appearance, tag = null)
    push(tagged)
}

/**
 * A "no-operation" implementation of [NavBarAppearanceStack].
 * Useful in Compose Previews.
 */
object NoopNavBarAppearanceStack : NavBarAppearanceStack {
    override fun push(appearance: NavBarAppearance.WithTag) {}
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
) {

    data class WithTag(
        val appearance: NavBarAppearance,
        val tag: Any?,
    )
}