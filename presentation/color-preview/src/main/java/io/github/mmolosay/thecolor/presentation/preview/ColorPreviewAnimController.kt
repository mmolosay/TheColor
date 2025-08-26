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

    val flowOfMainUiState: StateFlow<UiStateWithVisibility>
    val flowOfStableReachedUiState: StateFlow<UiState>

    fun onNewUiState(uiState: UiState)

    interface View {
        fun animateMainVisibility(command: AnimateVisibilityCommand)
        fun animateVisibleUiStateUpdate(command: AnimateVisibleUiStateUpdateCommand)
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

    override val flowOfMainUiState = MutableStateFlow(uiState.withVisibility())
    private var mainUiState by flowOfMainUiState.asDelegate()

    private val ongoingUpdatesOfVisibleUiState = mutableListOf<UiState.Visible>()

    override val flowOfStableReachedUiState =
        MutableStateFlow<UiState>(uiState) // initial state is stable; MutableStateFlow may conflate rapid updates
    private var stableReachedUiState by flowOfStableReachedUiState.asDelegate()

    private var ongoingMainAnimTarget: UiStateWithVisibility? = null
    private val isMainAnimRunning: Boolean
        get() = (ongoingMainAnimTarget != null)

    override fun onNewUiState(uiState: UiState) {
        val uiStateWithVisibility = uiState.withVisibility()
        if (view == null) {
            mainUiState = uiStateWithVisibility
            return // no view -> no animation -> immediate update
        }

        val view = requireNotNull(view)
        val command = uiStateWithVisibility.toAnimateCommand()
        view.animateMainVisibility(command)

        val newVisibilityDest = command.dest.visibility
        if (isMainAnimRunning) {
            if (newVisibilityDest == Visibility.Expanded) {
                uiState.emitAsUpdateOfVisibleUiState()
            }
        } else {
            val mainCurrentVisibility = mainUiState.visibility
            if (mainCurrentVisibility == Visibility.Collapsed) {
                // set mainUiState so it's visible during animation
                if (uiState is UiState.Visible) {
                    mainUiState = uiStateWithVisibility
                }
            }
            if (mainCurrentVisibility == Visibility.Expanded && newVisibilityDest == Visibility.Expanded) {
                uiState.emitAsUpdateOfVisibleUiState()
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
        view.animateVisibleUiStateUpdate(command)
    }

    private fun onUpdateAnimStarted(update: UiState.Visible) {
        ongoingUpdatesOfVisibleUiState += update
    }

    private fun onUpdateAnimFinished(update: UiState.Visible) {
        mainUiState = update.withVisibility()
        ongoingUpdatesOfVisibleUiState.remove(update).also { wasRemoved ->
            require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
        }
        if (ongoingUpdatesOfVisibleUiState.isEmpty() && !isMainAnimRunning) {
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
            onAnimStarted = { onMainVisibilityAnimStarted(dest = this) },
            onAnimFinished = { onMainVisibilityAnimFinished(reached = this.visibility) }
        )

    private fun onMainVisibilityAnimStarted(dest: UiStateWithVisibility) {
        ongoingMainAnimTarget = dest
    }

    private fun onMainVisibilityAnimFinished(reached: Visibility) {
        val ongoing = ongoingMainAnimTarget
        if (ongoing == null) return // no animation was running
        if (ongoing.visibility != reached) return // was not expected to be reached
        when (reached) {
            Visibility.Collapsed -> {
                check(ongoingUpdatesOfVisibleUiState.isEmpty()) { "main can't collapse while there are updates ongoing" }
                stableReachedUiState = ongoing.uiState
            }
            Visibility.Expanded -> {
                if (ongoingUpdatesOfVisibleUiState.isEmpty()) {
                    // only consider this UI state as stable if there's no updates ongoing
                    stableReachedUiState = ongoing.uiState
                }
            }
        }
        this.mainUiState = UiStateWithVisibility(uiState = ongoing.uiState, visibility = reached)
        this.ongoingMainAnimTarget = null
    }
}