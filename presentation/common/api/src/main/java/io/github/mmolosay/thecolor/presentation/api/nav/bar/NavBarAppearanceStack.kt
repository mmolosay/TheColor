package io.github.mmolosay.thecolor.presentation.api.nav.bar

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * An interface to work with a mutable stack of [NavBarAppearance]s.
 */
interface NavBarAppearanceStack {

    /**
     * A top-most (most recently [push]ed) appearance of the stack.
     */
    val topAppearanceFlow: StateFlow<NavBarAppearance.WithTag?>

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

    /**
     * Removes all appearances from this stack.
     */
    fun clear()
}

internal class NavBarAppearanceStackImpl : NavBarAppearanceStack {

    private val list = mutableListOf<NavBarAppearance.WithTag>()

    private val _topAppearanceFlow = MutableStateFlow<NavBarAppearance.WithTag?>(null)
    override val topAppearanceFlow = _topAppearanceFlow.asStateFlow()

    override fun push(appearance: NavBarAppearance.WithTag) =
        modifyStack {
            list += appearance
        }

    override fun peel() =
        modifyStack {
            list.removeLastOrNull()
        }

    override fun remove(tag: Any) =
        modifyStack {
            val indexOfLatestWithTag = list.indexOfLast { it.tag == tag }
            if (indexOfLatestWithTag != -1) {
                list.removeAt(indexOfLatestWithTag)
            }
        }

    override fun clear() =
        modifyStack {
            list.clear()
        }

    private inline fun modifyStack(block: () -> Unit) {
        block()
        val top = list.lastOrNull()
        _topAppearanceFlow.value = top
    }
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
    override val topAppearanceFlow = MutableStateFlow(null)
    override fun push(appearance: NavBarAppearance.WithTag) {}
    override fun peel() {}
    override fun remove(tag: Any) {}
    override fun clear() {}
}