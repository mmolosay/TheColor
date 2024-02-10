package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode as DomainMode

/**
 * Platform-agnostic data provided by ViewModel to color scheme View.
 */
data class ColorSchemeData(
    val swatches: List<ColorInt>,
    val activeMode: DomainMode, // it's ok to use domain model if it doesn't require mapping to presentation
    val selectedMode: DomainMode,
    val onModeSelect: (DomainMode) -> Unit,
    val activeSwatchCount: SwatchCount,
    val selectedSwatchCount: SwatchCount,
    val onSwatchCountSelect: (SwatchCount) -> Unit,
    val applyChangesButton: ApplyChangesButton,
) {

    /**
     * Solid color in RRGGBB format without alpha channel.
     */
    @JvmInline
    value class ColorInt(val hex: Int)

    /**
     * Options of swatch count.
     *
     * Unlike [DomainMode] that belongs to the domain layer,
     * this component belongs to the presentation layer.
     *
     * Set of possible [DomainMode]s is fixed.
     * Meanwhile, swatch count can be any [Int], and it's purely on presentation layer,
     * what options to give to the user.
     */
    enum class SwatchCount(val value: Int) {
        Three(3),
        Four(4),
        Six(6),
        Nine(9),
        Thirteen(13),
        Eighteen(18),
    }

    sealed interface ApplyChangesButton {
        data object Hidden : ApplyChangesButton
        data class Visible(val onClick: () -> Unit) : ApplyChangesButton
    }
}