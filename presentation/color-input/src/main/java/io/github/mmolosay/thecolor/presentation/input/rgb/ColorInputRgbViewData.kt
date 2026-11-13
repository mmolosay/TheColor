package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission

data class ColorInputRgbViewData(
    val answeredSubmission: ColorInputSubmission.Answered?,
    val isSmartBackspaceEnabled: Boolean,
)