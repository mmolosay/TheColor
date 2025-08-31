package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.UiStateWithVisibility
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.github.mmolosay.thecolor.utils.asDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

// "interface" for better visibility of exposed API
// used 'abstract class' instead of 'interface' because 'internal' modifier is not supported for members of 'interface'.
/**
 * A controller that contains logic of 'Color Preview' animations.
 *
 * There are two types of API in this component: internal and public.
 * Public API (such as [onNewUiState]) is used by a caller (component that created this controller).
 * Internal API (such as [setView]) is used by a 'Color Preview' View.
 *
 * The common use case for this controller is following:
 * 1. Parent of the 'Color Preview' View creates an instance of this controller.
 * 2. Parent passes the instance of the controller to the 'Color Preview' View.
 * 3. Parent starts emitting new [UiState]s to this controller via [onNewUiState].
 * 4. 'Color Preview' View creates an instance of [View] and sets it via [setView].
 * 5. Controller processes [UiState]s and calls corresponding methods of [View] to run animations on UI.
 *
 * Due to controller being created on the caller's side, the 'Color Preview' View may not be present
 * right away to animate incoming [UiState]s. For this reason, the 'Color Preview' View should
 * create an instance of [View] and set it to the controller using [setView].
 * Until the [View] is set, all incoming updates of [UiState] won't be translated to the animations
 * and will be applied straight away.
 */
abstract class ColorPreviewAnimController {
    internal abstract val flowOfUiState: StateFlow<UiStateWithVisibility>
    internal abstract val flowOfStableReachedUiState: StateFlow<UiState>

    internal abstract fun setView(view: View?)
    abstract fun onNewUiState(newUiState: UiState)

    internal interface View {
        fun animateVisibility(
            dest: UiStateWithVisibility,
            onAnimStarted: () -> Unit,
            onAnimFinished: () -> Unit,
        )

        fun animateUpdateOfVisibleUiState(
            uiState: UiState.Visible,
            onAnimStarted: () -> Unit,
            onAnimFinished: () -> Unit,
        )
    }

    /** Couples [uiState] with [visibility] derived from it. */
    internal data class UiStateWithVisibility(
        val uiState: UiState,
        val visibility: Visibility,
    )
}

internal val ColorPreviewAnimController.uiStateWithVisibility: UiStateWithVisibility
    get() = this.flowOfUiState.value

internal val ColorPreviewAnimController.lastStableReachedUiState: UiState
    get() = this.flowOfStableReachedUiState.value

object ColorPreviewAnimState {
    enum class Visibility {
        Collapsed, Expanded;
    }
}

// constructor function to hide type of impl
fun ColorPreviewAnimController(uiState: UiState): ColorPreviewAnimController =
    ColorPreviewAnimControllerImpl(uiState)

private class ColorPreviewAnimControllerImpl(
    uiState: UiState,
) : ColorPreviewAnimController() {

    private var view: View? = null

    override val flowOfUiState = MutableStateFlow(uiState.withVisibility())
    private var uiState by flowOfUiState.asDelegate()
    private var lastUiState = this.uiState

    override val flowOfStableReachedUiState =
        MutableStateFlow<UiState>(uiState) // initial state is stable; MutableStateFlow may conflate rapid updates
    private var stableReachedUiState by flowOfStableReachedUiState.asDelegate()

    private var ongoingVisibilityAnimTarget: UiStateWithVisibility? = null
    private val isVisibilityAnimRunning: Boolean
        get() = (ongoingVisibilityAnimTarget != null)

    private val ongoingUpdatesOfVisibleUiState = mutableListOf<UiState.Visible>()

    override fun setView(view: View?) {
        if (this.view != view) kotlin.run clearOngoingAnimations@{
            ongoingVisibilityAnimTarget = null
            ongoingUpdatesOfVisibleUiState.clear()
            uiState = lastUiState
        }
        this.view = view
    }

    override fun onNewUiState(newUiState: UiState) {
        val newUiStateWithVisibility = newUiState.withVisibility()
        if (view == null) {
            uiState = newUiStateWithVisibility
            return // no view -> no animation -> immediate update
        }

        val view = requireNotNull(view)
        view.animateVisibility(dest = newUiStateWithVisibility)

        val currentVisibility = uiState.visibility
        val newVisibilityDest = newUiStateWithVisibility.visibility
        if (isVisibilityAnimRunning) {
            if (newVisibilityDest == Visibility.Expanded) {
                view.animateUpdateOfVisibleUiState(uiState = newUiState as UiState.Visible) // transitive assumption based on new Visibility dest being 'Expanded'
            }
        } else {
            if (currentVisibility == Visibility.Expanded && newVisibilityDest == Visibility.Expanded) {
                view.animateUpdateOfVisibleUiState(uiState = newUiState as UiState.Visible) // transitive assumption based on new Visibility dest being 'Expanded'
            }
        }
        lastUiState = newUiStateWithVisibility
    }

    private fun View.animateVisibility(dest: UiStateWithVisibility) =
        this.animateVisibility(
            dest = dest,
            onAnimStarted = { onVisibilityAnimStarted(dest = dest) },
            onAnimFinished = { onVisibilityAnimFinished(reached = dest) },
        )

    private fun onVisibilityAnimStarted(dest: UiStateWithVisibility) {
        ongoingVisibilityAnimTarget = dest
    }

    private fun onVisibilityAnimFinished(reached: UiStateWithVisibility) {
        val ongoing = ongoingVisibilityAnimTarget
        if (ongoing == null) return // no animation was running
        if (ongoing.visibility != reached.visibility) return // was not expected to be reached

        this.ongoingVisibilityAnimTarget = null
        this.uiState = UiStateWithVisibility(uiState = ongoing.uiState, visibility = reached.visibility)
        if (isNoAnimRunning()) {
            stableReachedUiState = ongoing.uiState
        }
    }

    private fun View.animateUpdateOfVisibleUiState(uiState: UiState.Visible) =
        this.animateUpdateOfVisibleUiState(
            uiState = uiState,
            onAnimStarted = { onUpdateAnimStarted(update = uiState) },
            onAnimFinished = { onUpdateAnimFinished(update = uiState) },
        )

    private fun onUpdateAnimStarted(update: UiState.Visible) {
        ongoingUpdatesOfVisibleUiState += update
    }

    private fun onUpdateAnimFinished(update: UiState.Visible) {
        uiState = update.withVisibility()
        ongoingUpdatesOfVisibleUiState.remove(update).also { wasRemoved ->
            require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
        }
        if (isNoAnimRunning()) {
            stableReachedUiState = update
        }
    }

    private fun isNoAnimRunning(): Boolean {
        val visibilityAnimIsNotRunning = !isVisibilityAnimRunning
        val updatesOfVisibleUiStateAreNotRunning = ongoingUpdatesOfVisibleUiState.isEmpty()
        return (visibilityAnimIsNotRunning && updatesOfVisibleUiStateAreNotRunning)
    }

    private fun UiState.withVisibility() =
        UiStateWithVisibility(
            uiState = this,
            visibility = when (this) {
                is UiState.Hidden -> Visibility.Collapsed
                is UiState.Visible -> Visibility.Expanded
            },
        )
}