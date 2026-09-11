package io.github.mmolosay.thecolor.presentation.input.rgb

import kotlinx.coroutines.Job

sealed interface ColorInputRgbAction {

    data object SubmitInput : ColorInputRgbAction

    data object AckInputSubmissionResult : ColorInputRgbAction
}

typealias ExecuteColorInputRgbAction = (ColorInputRgbAction) -> Job