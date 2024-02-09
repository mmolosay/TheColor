package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.ColorScheme as DomainColorScheme

/**
 * Platform-agnostic data provided by ViewModel to color scheme View.
 */
data class ColorSchemeData(
    val swatches: List<ColorInt>,
    val activeMode: DomainColorScheme.Mode, // it's ok to use domain model if it doesn't require mapping to presentation
    val selectedMode: DomainColorScheme.Mode,
    val onModeSelect: (DomainColorScheme.Mode) -> Unit,
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

    @JvmInline
    value class SwatchCount(val value: Int)

    sealed interface ApplyChangesButton {
        data object Hidden : ApplyChangesButton
        data class Visible(val onClick: () -> Unit) : ApplyChangesButton
    }
}