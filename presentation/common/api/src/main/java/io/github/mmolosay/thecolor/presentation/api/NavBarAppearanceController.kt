package io.github.mmolosay.thecolor.presentation.api

import io.github.mmolosay.thecolor.utils.CoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import java.util.Optional

/**
 * Controller of navigation bar's appearance.
 * It supports "layering" appearances, so that if the latest appearance is cancelled,
 * then the one that was before it will be used (as when going back to previous screen).
 */
// TODO: refactor: NavBarAppearanceController should NOT derive from NavBarAppearanceSubStack
class NavBarAppearanceController(
    private val coroutineScope: CoroutineScope,
) : NavBarAppearanceSubStack {

    private val appearanceStack = mutableListOf<NavBarAppearance.WithTag>()

    @Suppress("RemoveRedundantQualifierName")
    private val subControllers = mutableListOf<NavBarAppearanceController.WithTag>()
    private var subControllersCollectionJob: Job? = null

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

    override fun clear() {
        appearanceStack.clear()
        emitLatestAppearance()
    }

    // TODO: add unit tests
    override fun subStack(tag: Any): NavBarAppearanceSubStack {
        val newSubController = NavBarAppearanceController(
            coroutineScope = CoroutineScope(parent = coroutineScope),
        )
        subControllers += WithTag(controller = newSubController, tag = tag)
        val mergedFlowsOfSubControllers: Flow<NavBarAppearance.WithTag?> =
            subControllers.map { it.controller.appearanceFlow }
                .toTypedArray()
                .let { merge(*it) }
        subControllersCollectionJob?.cancel()
        subControllersCollectionJob = coroutineScope.launch {
            collectMergedFlowsOfSubControllers(mergedFlowsOfSubControllers)
        }
        return newSubController
    }

    private fun emitLatestAppearance() {
        val latest = appearanceStack.lastOrNull()
        _appearanceFlow.value = latest
    }

    private suspend fun collectMergedFlowsOfSubControllers(
        mergedFlows: Flow<NavBarAppearance.WithTag?>,
    ) {
        mergedFlows.collect { subControllerAppearance ->
            val ownAppearance = appearanceFlow.value
            val newAppearance = if (ownAppearance != null && subControllerAppearance == null) {
                ownAppearance
            } else {
                subControllerAppearance
            }
            _appearanceFlow.value = newAppearance
        }
    }

    private data class WithTag(
        val controller: NavBarAppearanceController,
        val tag: Any,
    )
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

    /**
     * Creates a [NavBarAppearanceStack] that can be used by a component to [push] and [peel]
     * appearances safely.
     * For example, once component is removed, and it wants to remove all appearances it has [push]ed,
     * it may do so using [NavBarAppearanceSubStack.clear].
     */
    fun subStack(tag: Any): NavBarAppearanceSubStack
}

/**
 * A derivative from [NavBarAppearanceStack].
 * Serves as a separate allocated stack to be used by only one client (component).
 *
 * Contains mass-control methods that would've been too dangerous to have in regular [NavBarAppearanceStack].
 */
interface NavBarAppearanceSubStack : NavBarAppearanceStack {

    /**
     * Removes all appearances from this sub-stack.
     */
    fun clear()
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
    override fun subStack(tag: Any) = NoopNavBarAppearanceSubStack
}

/**
 * A "no-operation" implementation of [NavBarAppearanceSubStack].
 * Useful in Compose Previews.
 */
object NoopNavBarAppearanceSubStack :
    NavBarAppearanceSubStack,
    NavBarAppearanceStack by NoopNavBarAppearanceStack {
    override fun clear() {}
}

/**
 * Platform-agnostic model of navigation bar's appearance.
 *
 * @param color a color integer in `ARGB` format.
 * @param useLightTintForControls whether the controls should be light to contrast against dark [color].
 */
data class NavBarAppearance(
    val color: Optional<Int>,
    val useLightTintForControls: Optional<Boolean>,
) {

    data class WithTag(
        val appearance: NavBarAppearance,
        val tag: Any?,
    )
}

/**
 * A builder function for [NavBarAppearance] with values by default.
 */
fun navBarAppearance(
    color: Optional<Int> = Optional.empty(),
    useLightTintForControls: Optional<Boolean> = Optional.empty(),
) =
    NavBarAppearance(
        color = color,
        useLightTintForControls = useLightTintForControls,
    )

infix fun NavBarAppearance.withTag(tag: Any?): NavBarAppearance.WithTag =
    NavBarAppearance.WithTag(appearance = this, tag = tag)