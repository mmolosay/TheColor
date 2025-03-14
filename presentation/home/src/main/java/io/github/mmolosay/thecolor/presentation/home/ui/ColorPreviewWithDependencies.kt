package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import kotlinx.coroutines.flow.StateFlow

internal fun interface ColorPreviewComposable {
    @Composable
    operator fun invoke(data: ColorPreviewData)
}

/**
 * Contains 'Color Preview' [composable] and dependencies that it requires.
 */
internal interface ColorPreviewWithDependencies {
    val composable: ColorPreviewComposable
    val dataFlow: StateFlow<ColorPreviewData>
}

// constructor function, like 'StateFlow()'
internal fun ColorPreviewWithDependencies(
    dataFlow: StateFlow<ColorPreviewData>,
    composable: ColorPreviewComposable, // last to enabled trailing lambda syntax
): ColorPreviewWithDependencies =
    ColorPreviewWithDependenciesImpl(
        composable = composable,
        dataFlow = dataFlow,
    )

private data class ColorPreviewWithDependenciesImpl(
    override val composable: ColorPreviewComposable,
    override val dataFlow: StateFlow<ColorPreviewData>,
) : ColorPreviewWithDependencies

internal class NoopColorPreviewWithDependencies(
    override val composable: ColorPreviewComposable,
) : ColorPreviewWithDependencies {
    override val dataFlow: StateFlow<ColorPreviewData>
        get() = error("no-op")
}