package io.github.mmolosay.thecolor.presentation.input.rgb

sealed interface ColorInputRgbAction {

    data object SubmitInput : ColorInputRgbAction

    data object AckInputSubmissionResult : ColorInputRgbAction
}