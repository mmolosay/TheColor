package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.ui.graphics.Color

/**
 * Framework-oriented data required for color preview View to be presented by Compose.
 * It is a derivative from [ColorPreviewData].
 */
sealed interface ColorPreviewUiState {
    data object Hidden : ColorPreviewUiState
    data class Visible(val color: Color) : ColorPreviewUiState
}