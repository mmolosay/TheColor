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

    fun derive(
        colorPreview: ColorPreview = this.colorPreview,
        colorCenter: ColorCenter = this.colorCenter,
    ) = copy(colorPreview, colorCenter)

    companion object {
        val Start = HomeAnimState(
            colorPreview = ColorPreview.Initial,
            colorCenter = ColorCenter.Collapsed,
        )
        val Finished = HomeAnimState(
            colorPreview = ColorPreview.Dived,
            colorCenter = ColorCenter.Expanded,
        )
    }
}

/**
 * Produces initial [HomeAnimState] to be used when 'Home' View has just been displayed.
 */
internal fun initialHomeAnimState(
    isColorProceededWith: Boolean,
) =
    when (isColorProceededWith) {
        false -> HomeAnimState(
            colorPreview = HomeAnimState.ColorPreview.Initial,
            colorCenter = HomeAnimState.ColorCenter.Collapsed,
        )
        true -> HomeAnimState(
            colorPreview = HomeAnimState.ColorPreview.Dived,
            colorCenter = HomeAnimState.ColorCenter.Expanded,
        )
    }