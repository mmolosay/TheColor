package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.UpdateOfVisibleUiState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityWithCause
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import io.github.mmolosay.thecolor.utils.requireEmit
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

/*
 * This class takes different approach than `HomeAnimController`:
 * while latter exposes data to View via platform-agnostic Flows,
 * this class does so via Compose-specific State.
 */
// interface for better visibility of exposed API
interface ColorPreviewAnimController {
    val mainUiState: UiState

    // delivering values via Flow + collect() is quicker than via State + LaunchedEffect()
    val flowOfVisibilityDest: StateFlow<VisibilityWithCause>
    val visibilityDest: VisibilityWithCause
        get() = flowOfVisibilityDest.value

    val flowOfStableReachedUiState: Flow<UiState>

    val updatesOfVisibleUiState: List<UpdateOfVisibleUiState>

    fun onNewUiState(uiState: UiState)
    /*internal*/ fun onMainVisibilityAnimStarted(dest: VisibilityWithCause)
    /*internal*/ fun onMainVisibilityAnimFinished(reached: Visibility)
    /*internal*/ fun onUpdateAnimFinished(update: UpdateOfVisibleUiState)

    /** Couples [Visibility] with the [UiState] that [cause]d it. */
    data class VisibilityWithCause(
        val value: Visibility,
        val cause: UiState,
    )

    /** A `visible` [UiState] that arrived when the [mainUiState] was also `visible`. */
    data class UpdateOfVisibleUiState(
        val uiState: UiState.Visible,
        val id: Int,
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
    override var mainUiState by mutableStateOf(uiState)

    override val flowOfVisibilityDest = MutableStateFlow(value = uiState.toVisibilityWithCause())
    override val updatesOfVisibleUiState = mutableStateListOf<UpdateOfVisibleUiState>()

    override val flowOfStableReachedUiState = MutableSharedFlow<UiState>(
        replay = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )

    private var ongoingMainAnimTarget: VisibilityWithCause? = null
    private val isMainAnimRunning: Boolean
        get() = (ongoingMainAnimTarget != null)

    init {
        flowOfStableReachedUiState.requireEmit(uiState) // initial state is stable
    }

    override fun onNewUiState(uiState: UiState) {
        val newVisibilityWithCause = uiState.toVisibilityWithCause()
        val newVisibilityDest = newVisibilityWithCause.value
        flowOfVisibilityDest.value = newVisibilityWithCause
        if (isMainAnimRunning) {
            if (newVisibilityDest == Visibility.Expanded) {
                uiState.addAsUpdateOfVisibleUiState()
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
                uiState.addAsUpdateOfVisibleUiState()
            }
        }
    }

    override fun onMainVisibilityAnimStarted(dest: VisibilityWithCause) {
        ongoingMainAnimTarget = dest
    }

    override fun onMainVisibilityAnimFinished(reached: Visibility) {
        val ongoing = ongoingMainAnimTarget
        if (ongoing == null) return // no animation was running
        if (ongoing.value != reached) return // was not expected to be reached
        when (reached) {
            Visibility.Collapsed -> {
                check(updatesOfVisibleUiState.isEmpty()) { "main can't collapse while there are updates ongoing" }
                flowOfStableReachedUiState.requireEmit(ongoing.cause)
            }
            Visibility.Expanded -> {
                if (updatesOfVisibleUiState.isEmpty()) {
                    // only consider this UI state as stable if there's no updates ongoing
                    flowOfStableReachedUiState.requireEmit(ongoing.cause)
                }
            }
        }
        this.mainUiState = visibilityDest.cause
        this.ongoingMainAnimTarget = null
    }

    override fun onUpdateAnimFinished(update: UpdateOfVisibleUiState) {
        mainUiState = update.uiState
        updatesOfVisibleUiState.remove(update).also { wasRemoved ->
            require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
        }
        if (updatesOfVisibleUiState.isEmpty() && !isMainAnimRunning) {
            flowOfStableReachedUiState.requireEmit(update.uiState)
        }
    }

    private fun UiState.addAsUpdateOfVisibleUiState() {
        require(this is UiState.Visible)
        val id = updatesOfVisibleUiState.lastOrNull()?.id?.let { it + 1 } ?: 0
        val update = UpdateOfVisibleUiState(uiState = this, id = id)
        updatesOfVisibleUiState += update
    }

    private fun UiState.toVisibilityWithCause() =
        VisibilityWithCause(
            value = when (this) {
                is UiState.Hidden -> Visibility.Collapsed
                is UiState.Visible -> Visibility.Expanded
            },
            cause = this,
        )
}