package io.github.mmolosay.thecolor.presentation.home.ui

/**
 * State of UI animation of 'Home' View.
 */
internal data class HomeAnimState(
    val colorPreview: ColorPreview,
    val colorCenter: ColorCenter,
) {

    enum class ColorPreview {
        Initial, Dived;
    }

    enum class ColorCenter {
        Collapsed, Expanded;
    }
}

internal fun HomeAnimState(
    isColorProceededWith: Boolean,
) =
    HomeAnimState(
        colorPreview = when (isColorProceededWith) {
            false -> HomeAnimState.ColorPreview.Initial
            true -> HomeAnimState.ColorPreview.Dived
        },
        colorCenter = when (isColorProceededWith) {
            false -> HomeAnimState.ColorCenter.Collapsed
            true -> HomeAnimState.ColorCenter.Expanded
        },
    )

