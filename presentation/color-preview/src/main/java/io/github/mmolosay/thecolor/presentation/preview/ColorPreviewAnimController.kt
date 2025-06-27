package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController.VisibilityAnimDest
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimState.Visibility
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState as UiState

// interface for better visibility of exposed API
interface ColorPreviewAnimController {
    val latestUiState: UiState
    val mainUiState: UiState

    val flowOfVisibilityDest: StateFlow<VisibilityAnimDest>
    val visibilityDest: VisibilityAnimDest
        get() = flowOfVisibilityDest.value

    val flowOfVisibleUiStateUpdates: Flow<UiState.Visible>

    fun onVisibilityAnimFinished(reached: VisibilityAnimDest)
    fun onUpdateAnimFinished(uiState: UiState.Visible)
    fun onNewUiState(uiState: UiState)

    data class VisibilityAnimDest(
        val dest: Visibility,
        val cause: UiState,
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

    private val _flowOfVisibilityDest: MutableStateFlow<VisibilityAnimDest> =
        MutableStateFlow(value = uiState.toVisibilityAnimDest())
    override val flowOfVisibilityDest = _flowOfVisibilityDest.asStateFlow()

    private val _flowOfVisibleUiStateUpdates = MutableSharedFlow<UiState.Visible>(
        extraBufferCapacity = 1, onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    override val flowOfVisibleUiStateUpdates = _flowOfVisibleUiStateUpdates.asSharedFlow()

    override fun onVisibilityAnimFinished(reached: VisibilityAnimDest) {
        if (reached.cause is UiState.Hidden) {
            mainUiState = reached.cause
        }
    }

    override fun onUpdateAnimFinished(uiState: UiState.Visible) {
        mainUiState = uiState
    }

    override fun onNewUiState(uiState: UiState) {
        _flowOfVisibilityDest.value = uiState.toVisibilityAnimDest()
        if (mainUiState is UiState.Visible && uiState is UiState.Visible) {
            _flowOfVisibleUiStateUpdates.requireEmit(uiState)
        } else {
            // set mainUiState so it's visible during animation
            if (uiState is UiState.Visible) {
                mainUiState = uiState
            }
        }
        this.latestUiState = uiState
    }

    private fun UiState.toVisibilityAnimDest() =
        VisibilityAnimDest(
            dest = when (this) {
                is UiState.Hidden -> Visibility.Collapsed
                is UiState.Visible -> Visibility.Expanded
            },
            cause = this,
        )

    private fun <T> MutableSharedFlow<T>.requireEmit(value: T) {
        val wasEmitted = this.tryEmit(value)
        require(wasEmitted) { "value must be emitted" }
    }
}