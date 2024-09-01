package io.github.mmolosay.thecolor.presentation.input.impl.model

/**
 * Framework-oriented command to be handled by Color Input UI presented by Compose.
 */
sealed interface ColorInputUiCommand {
    val onExecuted: () -> Unit

    data class HideSoftwareKeyboard(
        override val onExecuted: () -> Unit,
    ) : ColorInputUiCommand
}