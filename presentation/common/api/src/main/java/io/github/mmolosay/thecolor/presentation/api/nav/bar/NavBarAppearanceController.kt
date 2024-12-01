package io.github.mmolosay.thecolor.presentation.api.nav.bar

import io.github.mmolosay.thecolor.utils.CoroutineScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch

/**
 * Controller of navigation bar's appearance.
 * It supports "layering" appearances, so that if the latest appearance is cancelled,
 * then the one that was before it will be used (as when going back to previous screen).
 */
interface NavBarAppearanceController : NavBarAppearanceControllerTree {
    val stack: NavBarAppearanceStack
}

/**
 * An initial nav bar appearance controller.
 * Can be considered as a root if looking on the controllers hierarchy as a tree-like structure.
 *
 * Doesn't have a [NavBarAppearanceStack] and only used to create child [NavBarAppearanceController]s.
 * This enforces each component to have it's own controller.
 */
class MainNavBarAppearanceController(
    private val coroutineScope: CoroutineScope,
) : NavBarAppearanceControllerTree {

    private val _appearanceFlow = MutableStateFlow<NavBarAppearance.WithTag?>(null)
    val appearanceFlow = _appearanceFlow.asStateFlow()

    private val controllerTree = NavBarAppearanceControllerTreeImpl(
        controllerFactory = ::newController
    )
    private var childrenCollectionJob: Job? = null

    // TODO: add unit tests
    override fun branch(tag: Any): NavBarAppearanceController {
        val child = controllerTree.branch(tag)
        childrenCollectionJob?.cancel()
        childrenCollectionJob = coroutineScope.launch {
            controllerTree.mergedFlowOfChildAppearances().collect { subStackAppearance ->
                _appearanceFlow.value = subStackAppearance
            }
        }
        return child
    }

    private fun newController(tag: Any): NavBarAppearanceController =
        NavBarAppearanceControllerImpl(
            tag = tag,
            coroutineScope = CoroutineScope(parent = coroutineScope),
        )
}

private class NavBarAppearanceControllerImpl(
    @Suppress("unused") private val tag: Any, // makes debugging easier
    private val coroutineScope: CoroutineScope,
) : NavBarAppearanceController {

    override val stack: NavBarAppearanceStack = NavBarAppearanceStackImpl()

    private val _appearanceFlow = MutableStateFlow<NavBarAppearance.WithTag?>(null)
    val appearanceFlow = _appearanceFlow.asStateFlow()

    private val controllerTree = NavBarAppearanceControllerTreeImpl(
        controllerFactory = ::newController
    )
    private var childrenCollectionJob: Job? = null

    override fun branch(tag: Any): NavBarAppearanceController {
        val child = controllerTree.branch(tag)
        childrenCollectionJob?.cancel()
        childrenCollectionJob = coroutineScope.launch {
            controllerTree.mergedFlowOfChildAppearances().collect { subStackAppearance ->
                _appearanceFlow.value = subStackAppearance
            }
        }
        return child
    }

    private fun newController(tag: Any): NavBarAppearanceController =
        NavBarAppearanceControllerImpl(
            tag = tag,
            coroutineScope = CoroutineScope(parent = coroutineScope),
        )
}

private class NavBarAppearanceControllerTreeImpl(
    private val controllerFactory: (tag: Any) -> NavBarAppearanceController,
) : NavBarAppearanceControllerTree {

    private val children = mutableListOf<NavBarAppearanceController>()

    override fun branch(tag: Any): NavBarAppearanceController {
        val child = controllerFactory(tag)
        children += child
        return child
    }

    fun mergedFlowOfChildAppearances(): Flow<NavBarAppearance.WithTag?> =
        children.map { (it as NavBarAppearanceControllerImpl).appearanceFlow }
            .toTypedArray()
            .let { merge(*it) }
            .drop(children.size) // replayed values from merged StateFlows
}

/**
 * A "no-operation" implementation of [NavBarAppearanceStack].
 * Useful in Compose Previews.
 */
object NoopNavBarAppearanceController : NavBarAppearanceController {
    override val stack = NoopNavBarAppearanceStack
    override fun branch(tag: Any): NavBarAppearanceController =
        NoopNavBarAppearanceController
}