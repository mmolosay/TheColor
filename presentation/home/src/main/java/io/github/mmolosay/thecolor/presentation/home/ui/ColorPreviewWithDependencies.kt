package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiStateController
import kotlinx.coroutines.flow.StateFlow

// TODO: remove if unused

/** Contains 'Color Preview' [composable] with dependencies that were passed to it. */
internal interface ColorPreviewWithDependencies {
    val dataFlow: StateFlow<ColorPreviewData>
    val uiStateController: ColorPreviewUiStateController
    val composable: @Composable () -> Unit
}

internal class NoopColorPreviewWithDependencies(
    composable: @Composable () -> Unit,
) : ColorPreviewWithDependencies {
    override val dataFlow: StateFlow<ColorPreviewData>
        get() = error("no-op")
    override val uiStateController: ColorPreviewUiStateController
        get() = error("no-op")
    override val composable = composable
}

internal data class ColorPreviewWithDependenciesImpl(
    override val dataFlow: StateFlow<ColorPreviewData>,
    override val uiStateController: ColorPreviewUiStateController,
    override val composable: @Composable () -> Unit,
) : ColorPreviewWithDependencies