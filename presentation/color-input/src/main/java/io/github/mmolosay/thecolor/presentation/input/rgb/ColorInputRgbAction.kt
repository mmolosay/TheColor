package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.common.ExecuteAction

sealed interface ColorInputRgbAction {

    data object SubmitInput : ColorInputRgbAction

    data object AckInputSubmissionResult : ColorInputRgbAction
}

typealias ExecuteColorInputRgbAction = ExecuteAction<ColorInputRgbAction>