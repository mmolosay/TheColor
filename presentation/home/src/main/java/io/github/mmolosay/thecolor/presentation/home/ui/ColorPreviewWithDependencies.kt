package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import kotlinx.coroutines.flow.StateFlow

internal fun interface ColorPreviewComposable {

    @Composable
    operator fun invoke(params: Params)

    data class Params(
        val animController: ColorPreviewAnimController,
        val onUiStateReached: (ColorPreviewUiState) -> Unit,
    )
}

/**
 * Contains 'Color Preview' [composable] and dependencies that it requires.
 */
internal data class ColorPreviewWithDependencies(
    val composable: ColorPreviewComposable,
    val dataFlow: StateFlow<ColorPreviewData?>,
)