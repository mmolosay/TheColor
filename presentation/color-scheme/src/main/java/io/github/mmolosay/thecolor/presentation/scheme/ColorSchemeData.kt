package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.Models
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode as DomainMode

/**
 * Platform-agnostic data provided by ViewModel to color scheme View.
 * Assembled from [Models] and lambda actions that are private to ViewModel.
 *
 * Such separation is required, because [Models] and action originate from different places.
 * [Models] are created by mapping domain models to presentation counterparts.
 * Actions belong to ViewModel and should only be exposed to View as a part of [ColorSchemeData].
 */
data class ColorSchemeData(
    val swatches: List<Swatch>,
    val onSwatchClick: (index: Int) -> Unit, // TODO: "click" and other View-related terms shouldn't appear in ViewModel data
    val activeMode: DomainMode,
    val selectedMode: DomainMode,
    val onModeSelect: (DomainMode) -> Unit,
    val activeSwatchCount: SwatchCount,
    val selectedSwatchCount: SwatchCount,
    val onSwatchCountSelect: (SwatchCount) -> Unit,
    val changes: Changes,
) {

    data class Swatch(
        val color: ColorInt,
        val isDark: Boolean,
    )

    data class Models(
        val swatches: List<Swatch>,
        val activeMode: DomainMode, // it's ok to use domain model if it doesn't require mapping to presentation
        val selectedMode: DomainMode,
        val activeSwatchCount: SwatchCount,
        val selectedSwatchCount: SwatchCount,
    )

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