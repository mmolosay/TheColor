package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.toCompose

fun ColorPreviewUiData(
    data: ColorPreviewData,
): ColorPreviewUiData =
    ColorPreviewUiData(
        color = data.color.toCompose(),
    )