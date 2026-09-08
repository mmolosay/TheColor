package io.github.mmolosay.thecolor.presentation.home.ui

import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.toUiState

/**
 * Describes UI state of 'Home' View.
 * Used to infer appropriate animation sequence / state.
 */
internal data class HomeUiState(
    val isColorPreviewVisible: Boolean,
    val isColorCenterVisible: Boolean,
)

internal fun HomeUiState.toAnimState(): HomeAnimState? =
    HomeAnimState(
        isColorPreviewVisible = this.isColorPreviewVisible,
        isColorCenterVisible = this.isColorCenterVisible,
    )

internal fun HomeState.toUiState(): HomeUiState {
    val isColorPreviewVisible = (this.tree.colorPreview.toUiState() is ColorPreviewUiState.Visible)
    val isColorCenterVisible = (this.tree.home.proceedResult is ProceedResult.Success)
    return HomeUiState(
        isColorPreviewVisible = isColorPreviewVisible,
        isColorCenterVisible = isColorCenterVisible,
    )
}