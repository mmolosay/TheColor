package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexUiCommand.ToggleSoftwareKeyboard
import io.github.mmolosay.thecolor.presentation.input.impl.hex.ColorInputHexUiCommand.ToggleSoftwareKeyboard.KeyboardState
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState

/**
 * Creates a list of [ColorInputHexUiCommand]s calculated as difference between two
 * consecutive [DataState]s.
 *
 * One can think of this as an opposite process of reducing event to state:
 * `stateOld + event = stateNew`, thus
 * `event = stateNew - stateOld`.
 */
internal fun ColorInputHexUiCommands(
    current: DataState<ColorInputHexData>,
    previous: DataState<ColorInputHexData>?,
): List<ColorInputHexUiCommand> =
    buildList {
        toggleSoftwareKeyboardCommandOrNull(current, previous)
            ?.also { add(it) }
    }

private fun toggleSoftwareKeyboardCommandOrNull(
    current: DataState<ColorInputHexData>,
    previous: DataState<ColorInputHexData>?,
): ToggleSoftwareKeyboard? {
    if (current !is DataState.Ready) return null
    if (previous !is DataState.Ready) return null
    val currentResult = current.data.colorSubmissionResult ?: return null
    val resultHasChanged = (currentResult != previous.data.colorSubmissionResult)
    if (!resultHasChanged) return null
    val destState = when (currentResult.wasAccepted) {
        true -> KeyboardState.Hidden // color input was accepted, thus user probably won't change it and doesn't need keyboard
        false -> KeyboardState.Visible // color input was rejected, thus user will probably want to correct it and needs keyboard
    }
    return ToggleSoftwareKeyboard(
        destState = destState,
        onExecuted = currentResult.discard,
    )
}