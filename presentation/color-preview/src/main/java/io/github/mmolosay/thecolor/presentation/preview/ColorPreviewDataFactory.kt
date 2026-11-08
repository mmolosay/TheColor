package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorToColorIntUseCase
import javax.inject.Inject

class ColorPreviewDataFactory @Inject constructor(
    private val colorToColorInt: ColorToColorIntUseCase,
) {
    fun create(
        color: Color?,
    ): ColorPreviewData =
        ColorPreviewData(
            color = with(colorToColorInt) { color?.toColorInt() },
        )
}