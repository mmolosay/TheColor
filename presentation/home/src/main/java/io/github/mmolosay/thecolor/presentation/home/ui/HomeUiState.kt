package io.github.mmolosay.thecolor.presentation.home.ui

import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.toUiState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.stateIn

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

internal fun FlowOfHomeUiState(
    coroutineScope: CoroutineScope,
    flowOfColorPreviewData: StateFlow<ColorPreviewData>,
    flowOfHomeData: StateFlow<HomeData>,
): StateFlow<HomeUiState> {
    fun actualUiState(): HomeUiState {
        val isColorPreviewVisible = run {
            val data = flowOfColorPreviewData.value
            return@run (data.toUiState() is ColorPreviewUiState.Visible)
        }
        val isColorCenterVisible = run {
            val data = flowOfHomeData.value
            return@run (data.proceedResult is ProceedResult.Success)
        }
        return HomeUiState(isColorPreviewVisible, isColorCenterVisible)
    }
    return combineTransform(
        flowOfColorPreviewData,
        flowOfHomeData,
    ) { _, _ ->
        // ignore collected values and assemble 'HomeUiState' from actual values of 'StateFlow's
        // that may have not yet been collected due to the asynchronous nature of Flows and coroutines
        emit(actualUiState())
    }.stateIn(coroutineScope, SharingStarted.Eagerly, initialValue = actualUiState())
}