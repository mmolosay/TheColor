package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.impl.colorint.ColorInt

/**
 * Describes how 'Color Preview' View is displayed in UI.
 * Platform-agnostic.
 * Derived from data provided by [ColorPreviewViewModel].
 *
 */
sealed interface ColorPreviewUiState {
    data object Hidden : ColorPreviewUiState
    data class Visible(val color: ColorInt) : ColorPreviewUiState
}

/**
 * Resolves how provided [ColorPreviewData] will be displayed in UI.
 * This behaviour must be backed up by [AnimatedColorPreview] composable (or other implementation of View).
 */
fun ColorPreviewData.toUiState(): ColorPreviewUiState =
    when (this.hasColor) {
        true -> ColorPreviewUiState.Visible(color = this.requireColor())
        false -> ColorPreviewUiState.Hidden
    }