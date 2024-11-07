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
        appearanceStack += appearance
        emitLatestAppearance()
    }

    override fun peel() {
        appearanceStack.removeLastOrNull()
        emitLatestAppearance()
    }

    override fun remove(tag: Any) {
        val indexOfLatestWithTag = appearanceStack.indexOfLast { it.tag == tag }
        if (indexOfLatestWithTag != -1) {
            appearanceStack.removeAt(indexOfLatestWithTag)
        }
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

    /**
     * Adds an [appearance] to the top of the stack.
     */
    fun push(appearance: NavBarAppearance.WithTag)

    /**
     * Removes an appearance from the top of the stack.
     * Does nothing if there's not a single appearance in the stack.
     */
    fun peel()

    /**
     * Removes first appearance with a [tag] searching top to bottom.
     * Does nothing if there's no such appearance in the stack.
     */
    fun remove(tag: Any)
}

/**
 * Adds an [appearance] with `null` tag to the top of the stack.
 */
fun NavBarAppearanceStack.push(appearance: NavBarAppearance) {
    val tagged = appearance withTag null
    push(tagged)
}

/**
 * A "no-operation" implementation of [NavBarAppearanceStack].
 * Useful in Compose Previews.
 */
object NoopNavBarAppearanceStack : NavBarAppearanceStack {
    override fun push(appearance: NavBarAppearance.WithTag) {}
    override fun peel() {}
    override fun remove(tag: Any) {}
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

infix fun NavBarAppearance.withTag(tag: Any?): NavBarAppearance.WithTag =
    NavBarAppearance.WithTag(appearance = this, tag = tag)