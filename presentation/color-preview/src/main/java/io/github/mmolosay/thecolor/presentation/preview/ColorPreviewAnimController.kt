package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.UiStateWithVisibility
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.View
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.github.mmolosay.thecolor.utils.asDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

// interface for better visibility of exposed API
interface ColorPreviewAnimController {
    val flowOfUiState: StateFlow<UiStateWithVisibility>
    val flowOfStableReachedUiState: StateFlow<UiState>

    /*internal*/ fun setView(view: View?)
    fun onNewUiState(newUiState: UiState)

    interface View {
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
    data class UiStateWithVisibility(
        val uiState: UiState,
        val visibility: Visibility,
    )
}

object ColorPreviewAnimState {
    enum class Visibility {
        Collapsed, Expanded;
    }
}

class ColorPreviewAnimControllerImpl(
    uiState: UiState,
) : ColorPreviewAnimController {

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
            if (currentVisibility == Visibility.Collapsed) {
                // set uiState so it's visible during animation
                if (newUiState is UiState.Visible) {
                    uiState = newUiStateWithVisibility
                }
            }
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
        when (reached.visibility) {
            Visibility.Collapsed -> {
                check(ongoingUpdatesOfVisibleUiState.isEmpty()) { "can't collapse while there are updates ongoing" }
                stableReachedUiState = ongoing.uiState
            }
            Visibility.Expanded -> {
                if (ongoingUpdatesOfVisibleUiState.isEmpty()) {
                    // only consider this UI state as stable if there's no updates ongoing
                    stableReachedUiState = ongoing.uiState
                }
            }
        }
        this.uiState = UiStateWithVisibility(uiState = ongoing.uiState, visibility = reached.visibility)
        this.ongoingVisibilityAnimTarget = null
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
        if (ongoingUpdatesOfVisibleUiState.isEmpty() && !isVisibilityAnimRunning) {
            stableReachedUiState = update
        }
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