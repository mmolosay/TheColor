package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiData.Preview
import io.github.mmolosay.thecolor.presentation.toCompose

fun ColorPreviewUiData(
    data: ColorPreviewData,
): ColorPreviewUiData =
    ColorPreviewUiData(
        preview = Preview(data),
    )

private fun Preview(data: ColorPreviewData) =
    when (data.color != null) {
        true -> Preview.Visible(color = data.color.toCompose())
        false -> Preview.Hidden
    }