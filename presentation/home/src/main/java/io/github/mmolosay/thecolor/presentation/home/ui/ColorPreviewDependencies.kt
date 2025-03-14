package io.github.mmolosay.thecolor.presentation.home.ui

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import kotlinx.coroutines.flow.StateFlow

/**
 * Contains dependencies that are required to display 'Color Preview'.
 */
internal interface ColorPreviewDependencies {
    val dataFlow: StateFlow<ColorPreviewData>
}

// constructor function, like 'StateFlow()'
internal fun ColorPreviewDependencies(
    dataFlow: StateFlow<ColorPreviewData>,
): ColorPreviewDependencies =
    ColorPreviewDependenciesImpl(
        dataFlow = dataFlow,
    )

private data class ColorPreviewDependenciesImpl(
    override val dataFlow: StateFlow<ColorPreviewData>,
) : ColorPreviewDependencies

internal object NoopColorPreviewDependencies : ColorPreviewDependencies {
    override val dataFlow: StateFlow<ColorPreviewData>
        get() = error("no-op")
}