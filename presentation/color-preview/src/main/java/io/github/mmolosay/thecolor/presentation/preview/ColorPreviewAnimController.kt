package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.UpdateOfVisibleUiState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityAnimDest
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

// interface for better visibility of exposed API
interface ColorPreviewAnimController {
    val latestUiState: UiState
    val mainUiState: UiState

    val flowOfVisibilityDest: StateFlow<VisibilityAnimDest>
    val visibilityDest: VisibilityAnimDest
        get() = flowOfVisibilityDest.value

    val flowOfVisibleUiStateUpdates: StateFlow<List<UpdateOfVisibleUiState>>

    fun onVisibilityAnimFinished(reached: VisibilityAnimDest)
    fun onNewUiState(uiState: UiState)
    fun onUpdateAnimFinished(update: UpdateOfVisibleUiState)

    data class VisibilityAnimDest(
        val dest: Visibility,
        val cause: UiState,
    )

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

    // TODO: abolish separation of Mutable/Immutable flows

    private val _flowOfVisibilityDest: MutableStateFlow<VisibilityAnimDest> =
        MutableStateFlow(value = uiState.toVisibilityAnimDest())
    override val flowOfVisibilityDest = _flowOfVisibilityDest.asStateFlow()

    private val _flowOfVisibleUiStateUpdates = MutableStateFlow<List<UpdateOfVisibleUiState>>(emptyList())
    override val flowOfVisibleUiStateUpdates = _flowOfVisibleUiStateUpdates.asStateFlow()

    override fun onVisibilityAnimFinished(reached: VisibilityAnimDest) {
        if (reached.cause is UiState.Hidden) {
            mainUiState = reached.cause
            _flowOfVisibleUiStateUpdates.update { emptyList() }
        }
    }

    override fun onNewUiState(uiState: UiState) {
        _flowOfVisibilityDest.value = uiState.toVisibilityAnimDest()
        if (mainUiState is UiState.Visible && uiState is UiState.Visible) {
            _flowOfVisibleUiStateUpdates.update { updates ->
                val id = updates.lastOrNull()?.id?.let { it + 1 } ?: 0
                val update = UpdateOfVisibleUiState(uiState, id)
                updates + update
            }
        } else {
            // set mainUiState so it's visible during animation
            if (uiState is UiState.Visible) {
                mainUiState = uiState
            }
        }
        this.latestUiState = uiState
    }

    override fun onUpdateAnimFinished(update: UpdateOfVisibleUiState) {
        mainUiState = update.uiState
        _flowOfVisibleUiStateUpdates.update { updates ->
            updates.toMutableList().apply {
                val wasRemoved = remove(update)
                require(wasRemoved) { "finished update wasn't in the list of the ongoing updates" }
            }
        }
    }

    private fun UiState.toVisibilityAnimDest() =
        VisibilityAnimDest(
            dest = when (this) {
                is UiState.Hidden -> Visibility.Collapsed
                is UiState.Visible -> Visibility.Expanded
            },
            cause = this,
        )
}