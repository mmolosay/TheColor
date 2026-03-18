package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt

/**
 * Platform-agnostic data provided by ViewModel to 'Color Details' View.
 *
 * Contains values regarding the color that is the currently selected in the 'Color Details' ViewModel.
 * This color is used to obtain the color details and usually referred to as "subject" color.
 */
data class SubjectColorData(
    val color: ColorInt,
    val isDark: Boolean,
)