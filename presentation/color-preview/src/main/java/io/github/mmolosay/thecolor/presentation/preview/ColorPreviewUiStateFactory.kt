package io.github.mmolosay.thecolor.presentation.preview

import io.github.mmolosay.thecolor.presentation.toCompose

fun ColorPreviewUiState(
    data: ColorPreviewData,
): ColorPreviewUiState =
    when (data.color != null) {
        true -> ColorPreviewUiState.Visible(color = data.color.toCompose())
        false -> ColorPreviewUiState.Hidden
    }