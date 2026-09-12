package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData
import io.github.mmolosay.thecolor.utils.Lens

/**
 * Platform-agnostic data provided by ViewModel to 'RGB Color Input' View.
 */
data class ColorInputRgbData(
    val rTextField: TextFieldData,
    val gTextField: TextFieldData,
    val bTextField: TextFieldData,
    val inputSubmissionResult: ColorInputSubmissionResult?,
    val isSmartBackspaceEnabled: Boolean,
)

internal object ColorInputRgbDataLenses {

    val rTextField = Lens<ColorInputRgbData, TextFieldData>(
        get = { s -> s.rTextField },
        set = { s, v -> s.copy(rTextField = v) },
    )

    val gTextField = Lens<ColorInputRgbData, TextFieldData>(
        get = { s -> s.gTextField },
        set = { s, v -> s.copy(gTextField = v) },
    )

    val bTextField = Lens<ColorInputRgbData, TextFieldData>(
        get = { s -> s.bTextField },
        set = { s, v -> s.copy(bTextField = v) },
    )
}