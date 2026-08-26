package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import io.github.mmolosay.thecolor.domain.color.ColorScheme
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to 'Color Scheme' View.
 */
data class ColorSchemeData(
    val swatches: List<Swatch>,
    val onSwatchSelect: (indexOfSwatch: Int) -> Unit,
    val activeMode: ColorScheme.Mode,
    val selectedMode: ColorScheme.Mode,
    val onModeSelect: (ColorScheme.Mode) -> Unit,
    val activeSwatchCount: SwatchCount,
    val selectedSwatchCount: SwatchCount,
    val onSwatchCountSelect: (SwatchCount) -> Unit,
    val changes: Changes,
) {

    data class Swatch(
        val color: ColorInt,
        val isDark: Boolean,
    )

    /**
     * Options of swatch count.
     *
     * Unlike [ColorScheme.Mode] that belongs to the domain layer,
     * this component belongs to the presentation layer.
     *
     * Set of possible [ColorScheme.Mode]s is fixed.
     * Meanwhile, swatch count can be any [Int], and it's purely on presentation layer,
     * what options to give to the user.
     * In other words, defined by UI design.
     */
    enum class SwatchCount(val value: Int) {
        Three(3),
        Four(4),
        Six(6),
        Nine(9),
        Thirteen(13),
        Eighteen(18),
    }

    sealed interface Changes {
        data object None : Changes
        data class Present(val applyChanges: () -> Unit) : Changes
    }
}