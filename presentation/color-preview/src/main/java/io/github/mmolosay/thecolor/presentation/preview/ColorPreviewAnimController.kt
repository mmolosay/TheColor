package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.AnimateVisibilityCommand
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.AnimateVisibleUiStateUpdateCommand
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.View
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityWithCause
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.github.mmolosay.thecolor.utils.asDelegate
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

// interface for better visibility of exposed API
interface ColorPreviewAnimController {
    /*internal*/ var view: View?

    val flowOfMainUiState: StateFlow<UiState>
    val flowOfMainVisibility: StateFlow<VisibilityWithCause>
    val flowOfStableReachedUiState: Flow<UiState>

    fun onNewUiState(uiState: UiState)

    interface View {
        fun animateMainVisibility(command: AnimateVisibilityCommand)
        fun animateVisibleUiStateUpdate(command: AnimateVisibleUiStateUpdateCommand)
    }

    data class VisibilityWithCause(
        val value: Visibility,
        val cause: UiState,
    )

    data class AnimateVisibilityCommand(
        val dest: VisibilityWithCause,
        val onAnimStarted: () -> Unit,
        val onAnimFinished: () -> Unit,
    )

    data class AnimateVisibleUiStateUpdateCommand(
        val uiState: UiState.Visible,
        val onAnimStarted: () -> Unit,
        val onAnimFinished: () -> Unit,
    )
}

val ColorPreviewAnimController.mainUiState: UiState
    get() = this.flowOfMainUiState.value

val ColorPreviewAnimController.mainVisibility: VisibilityWithCause
    get() = this.flowOfMainVisibility.value

object ColorPreviewAnimState {
    enum class Visibility {
        Collapsed, Expanded;
    }
}

class ColorPreviewAnimControllerImpl(
    uiState: UiState,
) : ColorPreviewAnimController {

    override var view: View? = null

    override val flowOfMainUiState = MutableStateFlow<UiState>(uiState)
    private var mainUiState by flowOfMainUiState.asDelegate()

    override val flowOfMainVisibility = MutableStateFlow(uiState.toVisibilityWithCause())

    private val ongoingUpdatesOfVisibleUiState = mutableListOf<UiState.Visible>()

    override val flowOfStableReachedUiState =
        MutableStateFlow<UiState>(uiState) // initial state is stable; MutableStateFlow may conflate rapid updates

    private var ongoingMainAnimTarget: VisibilityWithCause? = null
    private val isMainAnimRunning: Boolean
        get() = (ongoingMainAnimTarget != null)

    override fun onNewUiState(uiState: UiState) {
        if (view == null) {
            mainUiState = uiState
            return // no view -> no animation -> immediate update
        }

        val view = requireNotNull(view)
        val command = uiState.toVisibilityWithCause().toAnimateCommand()
        view.animateMainVisibility(command)

        val newVisibilityDest = command.dest.value
        if (isMainAnimRunning) {
            if (newVisibilityDest == Visibility.Expanded) {
                uiState.emitAsUpdateOfVisibleUiState()
            }
        } else {
            val mainCurrentVisibility = mainUiState.toVisibilityWithCause().value
            if (mainCurrentVisibility == Visibility.Collapsed) {
                // set mainUiState so it's visible during animation
                if (uiState is UiState.Visible) {
                    mainUiState = uiState
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
        mainUiState = update
        ongoingUpdatesOfVisibleUiState.remove(update).also { wasRemoved ->
            require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
        }
        if (ongoingUpdatesOfVisibleUiState.isEmpty() && !isMainAnimRunning) {
            flowOfStableReachedUiState.value = update
        }
    }

    private fun UiState.toVisibilityWithCause() =
        VisibilityWithCause(
            value = when (this) {
                is UiState.Hidden -> Visibility.Collapsed
                is UiState.Visible -> Visibility.Expanded
            },
            cause = this,
        )

    private fun VisibilityWithCause.toAnimateCommand() =
        AnimateVisibilityCommand(
            dest = this,
            onAnimStarted = { onMainVisibilityAnimStarted(dest = this) },
            onAnimFinished = { onMainVisibilityAnimFinished(reached = this.value) }
        )

    private fun onMainVisibilityAnimStarted(dest: VisibilityWithCause) {
        ongoingMainAnimTarget = dest
    }

    private fun onMainVisibilityAnimFinished(reached: Visibility) {
        val ongoing = ongoingMainAnimTarget
        if (ongoing == null) return // no animation was running
        if (ongoing.value != reached) return // was not expected to be reached
        when (reached) {
            Visibility.Collapsed -> {
                check(ongoingUpdatesOfVisibleUiState.isEmpty()) { "main can't collapse while there are updates ongoing" }
                flowOfStableReachedUiState.value = ongoing.cause
            }
            Visibility.Expanded -> {
                if (ongoingUpdatesOfVisibleUiState.isEmpty()) {
                    // only consider this UI state as stable if there's no updates ongoing
                    flowOfStableReachedUiState.value = ongoing.cause
                }
            }
        }
        this.mainUiState = ongoing.cause
        this.ongoingMainAnimTarget = null
    }
}