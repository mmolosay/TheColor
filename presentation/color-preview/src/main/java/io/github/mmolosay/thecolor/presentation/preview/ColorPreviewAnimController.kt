package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.AnimateVisibilityCommand
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.AnimateVisibleUiStateUpdateCommand
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.UiStateWithVisibility
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.View
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.github.mmolosay.thecolor.utils.asDelegate
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

// interface for better visibility of exposed API
interface ColorPreviewAnimController {
    /*internal*/ var view: View?

    val flowOfUiState: StateFlow<UiStateWithVisibility>
    val flowOfStableReachedUiState: StateFlow<UiState>

    fun onNewUiState(newUiState: UiState)

    interface View {
        fun animateVisibility(command: AnimateVisibilityCommand)
        fun animateUpdateOfVisibleUiState(command: AnimateVisibleUiStateUpdateCommand)
    }

    /** Couples [uiState] with [visibility] derived from it. */
    data class UiStateWithVisibility(
        val uiState: UiState,
        val visibility: Visibility,
    )

    data class AnimateVisibilityCommand(
        val dest: UiStateWithVisibility,
        val onAnimStarted: () -> Unit,
        val onAnimFinished: () -> Unit,
    )

    data class AnimateVisibleUiStateUpdateCommand(
        val uiState: UiState.Visible,
        val onAnimStarted: () -> Unit,
        val onAnimFinished: () -> Unit,
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

    override var view: View? = null

    override val flowOfUiState = MutableStateFlow(uiState.withVisibility())
    private var uiState by flowOfUiState.asDelegate()

    private val ongoingUpdatesOfVisibleUiState = mutableListOf<UiState.Visible>()

    override val flowOfStableReachedUiState =
        MutableStateFlow<UiState>(uiState) // initial state is stable; MutableStateFlow may conflate rapid updates
    private var stableReachedUiState by flowOfStableReachedUiState.asDelegate()

    private var ongoingVisibilityAnimTarget: UiStateWithVisibility? = null
    private val isVisibilityAnimRunning: Boolean
        get() = (ongoingVisibilityAnimTarget != null)

    override fun onNewUiState(newUiState: UiState) {
        val uiStateWithVisibility = newUiState.withVisibility()
        if (view == null) {
            uiState = uiStateWithVisibility
            return // no view -> no animation -> immediate update
        }

        val view = requireNotNull(view)
        val command = uiStateWithVisibility.toAnimateCommand()
        view.animateVisibility(command)

        val newVisibilityDest = command.dest.visibility
        if (isVisibilityAnimRunning) {
            if (newVisibilityDest == Visibility.Expanded) {
                newUiState.emitAsUpdateOfVisibleUiState()
            }
        } else {
            val currentVisibility = uiState.visibility
            if (currentVisibility == Visibility.Collapsed) {
                // set uiState so it's visible during animation
                if (newUiState is UiState.Visible) {
                    this@ColorPreviewAnimControllerImpl.uiState = uiStateWithVisibility
                }
            }
            if (currentVisibility == Visibility.Expanded && newVisibilityDest == Visibility.Expanded) {
                newUiState.emitAsUpdateOfVisibleUiState()
            }
        }
    }

    private fun UiState.emitAsUpdateOfVisibleUiState() {
        require(this is UiState.Visible)
        val command = AnimateVisibleUiStateUpdateCommand(
            uiState = this,
            onAnimStarted = { onUpdateAnimStarted(update = this) },
            onAnimFinished = { onUpdateAnimFinished(update = this) },
        )
        val view = requireNotNull(view)
        view.animateUpdateOfVisibleUiState(command)
    }

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

    private fun UiStateWithVisibility.toAnimateCommand() =
        AnimateVisibilityCommand(
            dest = this,
            onAnimStarted = { onVisibilityAnimStarted(dest = this) },
            onAnimFinished = { onVisibilityAnimFinished(reached = this.visibility) }
        )

    private fun onVisibilityAnimStarted(dest: UiStateWithVisibility) {
        ongoingVisibilityAnimTarget = dest
    }

    private fun onVisibilityAnimFinished(reached: Visibility) {
        val ongoing = ongoingVisibilityAnimTarget
        if (ongoing == null) return // no animation was running
        if (ongoing.visibility != reached) return // was not expected to be reached
        when (reached) {
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
        this.uiState = UiStateWithVisibility(uiState = ongoing.uiState, visibility = reached)
        this.ongoingVisibilityAnimTarget = null
    }
}