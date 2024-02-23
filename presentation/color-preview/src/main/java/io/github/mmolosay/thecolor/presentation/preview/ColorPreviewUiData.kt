package io.github.mmolosay.thecolor.presentation.preview

import androidx.compose.ui.graphics.Color

/**
 * Framework-oriented data required for color preview View to be presented by Compose.
 * It is a derivative from [ColorPreviewData].
 */
data class ColorPreviewUiData(
    val color: Color,
)