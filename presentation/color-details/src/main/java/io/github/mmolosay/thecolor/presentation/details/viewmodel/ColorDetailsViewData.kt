package io.github.mmolosay.thecolor.presentation.details.viewmodel

import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt

sealed interface ColorDetailsViewData {

    data object Empty : ColorDetailsViewData

    data class Loading(
        val subject: Subject,
    ) : ColorDetailsViewData

    data class Ready(
        val subject: Subject,
        val data: ColorDetailsData,
    ) : ColorDetailsViewData

    data class Error(
        val subject: Subject,
        val cause: Throwable,
    ) : ColorDetailsViewData

    /**
     * Contains values regarding the color that is the currently selected in the 'Color Details' feature.
     * This color is used to obtain the color details and usually referred to as "subject" color.
     */
    data class Subject(
        val color: ColorInt,
        val isDark: Boolean,
    )
}