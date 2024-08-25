package io.github.mmolosay.thecolor.presentation.input.impl.hex

/**
 * Framework-oriented command to be handled by Color Input UI presented by Compose.
 */
sealed interface ColorInputHexUiCommand {
    val onExecuted: () -> Unit

    data class HideSoftwareKeyboard(
        override val onExecuted: () -> Unit,
    ) : ColorInputHexUiCommand
}