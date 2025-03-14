package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState.MainState

/**
 * Describes current UI state of 'Color Preview' UI component.
 */
data class ColorPreviewUiState(
    val main: MainState,
) {
    sealed interface MainState {
        data object Hidden : MainState
        data class Visible(val color: ColorInt) : MainState
    }
}

internal fun ColorPreviewData.toUiState(): ColorPreviewUiState {
    val data = this
    val main = when (data.color != null) {
        true -> MainState.Visible(color = this.color)
        false -> MainState.Hidden
    }
    return ColorPreviewUiState(
        main = main,
    )
}