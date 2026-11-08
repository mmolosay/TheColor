package io.github.mmolosay.thecolor.presentation.scheme.viewmodel

import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import kotlinx.collections.immutable.ImmutableList
import io.github.mmolosay.thecolor.domain.color.ColorScheme as DomainColorScheme

/**
 * Platform-agnostic data provided by ViewModel to 'Color Scheme' View.
 */
data class ColorSchemeData(
    val swatches: ImmutableList<Swatch>,
    val activeMode: DomainColorScheme.Mode,
    val selectedMode: DomainColorScheme.Mode,
    val activeSwatchCount: SwatchCount,
    val selectedSwatchCount: SwatchCount,
    val hasChangesToApply: Boolean,
) {

    data class Swatch(
        val color: ColorInt,
        val isDark: Boolean,
    )
}