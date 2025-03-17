package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.api.ColorInt

/**
 * Describes how 'Color Preview' View is displayed in UI.
 * Platform-agnostic.
 */
sealed interface ColorPreviewUiState {
    data object Hidden : ColorPreviewUiState
    data class Visible(val color: ColorInt) : ColorPreviewUiState
}

internal fun ColorPreviewUiState.colorOrNull(): ColorInt? =
    (this as? ColorPreviewUiState.Visible)?.color

/**
 * Resolves how provided [ColorPreviewData] will be displayed in UI.
 * This behaviour must be backed up by [ColorPreview] composable (or other implementation of View).
 */
fun ColorPreviewData.toUiState(): ColorPreviewUiState =
    when (this.hasColor) {
        true -> ColorPreviewUiState.Visible(color = this.requireColor())
        false -> ColorPreviewUiState.Hidden
    }