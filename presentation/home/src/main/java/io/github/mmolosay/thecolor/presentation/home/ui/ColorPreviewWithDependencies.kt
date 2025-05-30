package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewViewModel

internal fun interface ColorPreviewComposable {
    @Composable
    operator fun invoke(
        uiState: ColorPreviewUiState,
        onAnimationFinished: (ColorPreviewUiState) -> Unit,
    )
}

/**
 * Contains 'Color Preview' [composable] and dependencies that it requires.
 */
internal sealed interface ColorPreviewWithDependencies {
    val composable: ColorPreviewComposable
    val viewModel: ColorPreviewViewModel
}

// constructor function, like 'StateFlow()'
internal fun ColorPreviewWithDependencies(
    viewModel: ColorPreviewViewModel,
    composable: ColorPreviewComposable, // last to enabled trailing lambda syntax
): ColorPreviewWithDependencies =
    ColorPreviewWithDependenciesImpl(
        composable = composable,
        viewModel = viewModel,
    )

private data class ColorPreviewWithDependenciesImpl(
    override val composable: ColorPreviewComposable,
    override val viewModel: ColorPreviewViewModel,
) : ColorPreviewWithDependencies

internal class NoopColorPreviewWithDependencies(
    override val composable: ColorPreviewComposable,
) : ColorPreviewWithDependencies {
    override val viewModel: ColorPreviewViewModel
        get() = error("no-op")
}