package io.github.mmolosay.thecolor.presentation.input.hex

import kotlinx.coroutines.Job

sealed interface ColorInputHexAction {

    data object SubmitInput : ColorInputHexAction

    data object AckInputSubmissionResult : ColorInputHexAction
}

typealias ExecuteColorInputHexAction = (ColorInputHexAction) -> Job