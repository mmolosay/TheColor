package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.UpdateOfVisibleUiState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityWithCause
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
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
    val latestUiState: UiState
    val mainUiState: UiState

    // delivering values via Flow + collect() is quicker than via State + LaunchedEffect()
    val flowOfVisibilityDest: StateFlow<VisibilityWithCause>
    val visibilityDest: VisibilityWithCause
        get() = flowOfVisibilityDest.value

    val updatesOfVisibleUiState: List<UpdateOfVisibleUiState>

    fun onNewUiState(uiState: UiState)
    /*internal*/ fun onVisibilityAnimFinished(reached: VisibilityWithCause)
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
    override var latestUiState by mutableStateOf(uiState)
    override var mainUiState by mutableStateOf(uiState)

    override val flowOfVisibilityDest = MutableStateFlow(value = uiState.toVisibilityWithCause())
    override val updatesOfVisibleUiState = mutableStateListOf<UpdateOfVisibleUiState>()

    override fun onNewUiState(uiState: UiState) {
        flowOfVisibilityDest.value = uiState.toVisibilityWithCause()
        if (mainUiState is UiState.Visible && uiState is UiState.Visible) {
            val id = updatesOfVisibleUiState.lastOrNull()?.id?.let { it + 1 } ?: 0
            val update = UpdateOfVisibleUiState(uiState, id)
            updatesOfVisibleUiState += update
        } else {
            // set mainUiState so it's visible during animation
            if (uiState is UiState.Visible) {
                mainUiState = uiState
            }
        }
        this.latestUiState = uiState
    }

    override fun onVisibilityAnimFinished(reached: VisibilityWithCause) {
        if (reached.cause is UiState.Hidden) {
            mainUiState = reached.cause
            updatesOfVisibleUiState.clear()
        }
    }

    override fun onUpdateAnimFinished(update: UpdateOfVisibleUiState) {
        mainUiState = update.uiState
        updatesOfVisibleUiState.remove(update).also { wasRemoved ->
            require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
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
}