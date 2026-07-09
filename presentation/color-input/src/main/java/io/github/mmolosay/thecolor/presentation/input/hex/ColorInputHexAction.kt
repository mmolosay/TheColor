package io.github.mmolosay.thecolor.presentation.input.hex

sealed interface ColorInputHexAction {

    data object SubmitInput : ColorInputHexAction

    data object AckInputSubmissionResult : ColorInputHexAction
}