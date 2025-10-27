package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.presentation.impl.colorint.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to 'Color Details' View.
 *
 * This model contains values regarding color that was submitted to 'Color Details' ViewModel.
 * This color is used to obtain color details and usually referred to as "seed".
 */
data class ColorDetailsSeedData(
    val color: ColorInt,
    val isDark: Boolean,
)