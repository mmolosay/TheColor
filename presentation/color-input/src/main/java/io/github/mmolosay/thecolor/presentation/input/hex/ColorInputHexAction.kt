package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.common.ExecuteAction

sealed interface ColorInputHexAction {

    data object SubmitInput : ColorInputHexAction

    data object AckInputSubmissionResult : ColorInputHexAction
}

typealias ExecuteColorInputHexAction = ExecuteAction<ColorInputHexAction>