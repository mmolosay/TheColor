package io.github.mmolosay.thecolor.presentation.input.impl.hex

/**
 * Framework-oriented command to be handled by Color Input UI presented by Compose.
 */
sealed interface ColorInputHexUiCommand {
    val onExecuted: () -> Unit

    data class ToggleSoftwareKeyboard(
        val destState: KeyboardState,
        override val onExecuted: () -> Unit,
    ) : ColorInputHexUiCommand {

        enum class KeyboardState {
            Visible, Hidden
        }
    }
}