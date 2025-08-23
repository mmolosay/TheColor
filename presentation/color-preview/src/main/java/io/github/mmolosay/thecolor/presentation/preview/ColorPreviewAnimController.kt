package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.AnimateVisibilityCommand
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityWithCause
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.github.mmolosay.thecolor.utils.requireEmit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

// interface for better visibility of exposed API
interface ColorPreviewAnimController {
    val flowOfMainUiState: StateFlow<UiState>
    val mainUiState: UiState
        get() = flowOfMainUiState.value

    val flowOfAnimateVisibilityCommand: StateFlow<AnimateVisibilityCommand>
    val animateVisibilityCommand: AnimateVisibilityCommand
        get() = flowOfAnimateVisibilityCommand.value

    val flowOfUpdatesOfVisibleUiState: SharedFlow<UiState.Visible>

    val flowOfStableReachedUiState: Flow<UiState>

    fun onNewUiState(uiState: UiState)

    // TODO: expose as part of some 'Command' object as for 'AnimateVisibilityCommand'
    /*internal*/ fun onUpdateAnimStarted(update: UiState.Visible)
    /*internal*/ fun onUpdateAnimFinished(update: UiState.Visible)

    data class VisibilityWithCause(
        val value: Visibility,
        val cause: UiState,
    )

    data class AnimateVisibilityCommand(
        val dest: VisibilityWithCause,
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
    override val flowOfMainUiState = MutableStateFlow<UiState>(uiState)
    override var mainUiState: UiState
        get() = super.mainUiState
        set(value) {
            flowOfMainUiState.value = value
        }

    override val flowOfAnimateVisibilityCommand =
        MutableStateFlow(value = uiState.toVisibilityWithCause().toAnimateCommand())

    override val flowOfUpdatesOfVisibleUiState = MutableSharedFlow<UiState.Visible>(
        extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    private val ongoingUpdatesOfVisibleUiState = mutableListOf<UiState.Visible>()

    override val flowOfStableReachedUiState =
        MutableStateFlow<UiState>(uiState) // initial state is stable; MutableStateFlow may conflate rapid updates

    private var ongoingMainAnimTarget: VisibilityWithCause? = null
    private val isMainAnimRunning: Boolean
        get() = (ongoingMainAnimTarget != null)

    override fun onNewUiState(uiState: UiState) {
        val newCommand = uiState.toVisibilityWithCause().toAnimateCommand()
        flowOfAnimateVisibilityCommand.value = newCommand

        val newVisibilityDest = newCommand.dest.value
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
        this.mainUiState = animateVisibilityCommand.dest.cause
        this.ongoingMainAnimTarget = null
    }

    override fun onUpdateAnimStarted(update: UiState.Visible) {
        ongoingUpdatesOfVisibleUiState += update
    }

    override fun onUpdateAnimFinished(update: UiState.Visible) {
        mainUiState = update
        ongoingUpdatesOfVisibleUiState.remove(update).also { wasRemoved ->
            require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
        }
        if (ongoingUpdatesOfVisibleUiState.isEmpty() && !isMainAnimRunning) {
            flowOfStableReachedUiState.value = update
        }
    }

    private fun UiState.emitAsUpdateOfVisibleUiState() {
        require(this is UiState.Visible)
        flowOfUpdatesOfVisibleUiState.requireEmit(this)
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
}