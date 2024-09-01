package io.github.mmolosay.thecolor.presentation.input.impl.hex

import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorInputUiCommand
import io.github.mmolosay.thecolor.presentation.input.impl.model.ColorInputUiCommand.HideSoftwareKeyboard
import io.github.mmolosay.thecolor.presentation.input.impl.model.DataState
import io.github.mmolosay.thecolor.presentation.input.impl.model.hideSoftwareKeyboardCommandOrNull

/**
 * Creates a list of [ColorInputUiCommand]s calculated as difference between two
 * consecutive [DataState]s.
 *
 * One can think of this as an opposite process of reducing event to state:
 * `stateOld + event = stateNew`, thus
 * `event = stateNew - stateOld`.
 */
internal fun ColorInputHexUiCommands(
    current: DataState<ColorInputHexData>,
    previous: DataState<ColorInputHexData>?,
): List<ColorInputUiCommand> =
    buildList {
        hideSoftwareKeyboardCommandOrNull(current, previous)
            ?.also { add(it) }
    }

private fun hideSoftwareKeyboardCommandOrNull(
    current: DataState<ColorInputHexData>,
    previous: DataState<ColorInputHexData>?,
): HideSoftwareKeyboard? {
    if (current !is DataState.Ready) return null
    if (previous !is DataState.Ready) return null
    val currentResult = current.data.colorSubmissionResult ?: return null
    val resultHasChanged = (currentResult != previous.data.colorSubmissionResult)
    if (resultHasChanged.not()) return null
    return hideSoftwareKeyboardCommandOrNull(currentResult)
}