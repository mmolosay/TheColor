package io.github.mmolosay.thecolor.presentation.home.ui

/**
 * State of UI animation of 'Home' View.
 */
internal data class HomeAnimState(
    val colorPreview: ColorPreview,
) {

    enum class ColorPreview {
        Initial, Dived;
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
    )