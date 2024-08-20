package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode as DomainMode

/**
 * Platform-agnostic data provided by ViewModel to color scheme View.
 */
data class ColorSchemeData(
    val swatches: List<Swatch>,
    val onSwatchSelect: (index: Int) -> Unit,
    val activeMode: DomainMode,
    val selectedMode: DomainMode,
    val onModeSelect: (DomainMode) -> Unit,
    val activeSwatchCount: SwatchCount,
    val selectedSwatchCount: SwatchCount,
    val onSwatchCountSelect: (SwatchCount) -> Unit,
    val changes: Changes,
) {

    data class Swatch(
        val color: io.github.mmolosay.thecolor.presentation.api.ColorInt,
        val isDark: Boolean,
    )

    /**
     * Options of swatch count.
     *
     * Unlike [DomainMode] that belongs to the domain layer,
     * this component belongs to the presentation layer.
     *
     * Set of possible [DomainMode]s is fixed.
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