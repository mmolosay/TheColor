package io.github.mmolosay.thecolor.presentation.input.impl.hex

import android.content.Context
import io.github.mmolosay.thecolor.presentation.input.impl.R
import io.github.mmolosay.thecolor.presentation.input.impl.field.TextFieldUiData

/**
 * Framework-oriented data required for HEX color input View to be presented by Compose.
 */
data class ColorInputHexUiData(
    val textField: TextFieldUiData,
    val onImeActionDone: () -> Unit,
    val toggleSoftwareKeyboardCommand: ToggleSoftwareKeyboardCommand?,
) {

    /**
     * A command to be handled by UI to hide/show software keyboard.
     * Use [onExecuted] to report that command was handled (processed).
     */
    data class ToggleSoftwareKeyboardCommand(
        val destState: KeyboardState,
        val onExecuted: () -> Unit,
    ) {
        enum class KeyboardState {
            Visible, Hidden
        }
    }

    /**
     * Part of to-be [ColorInputHexUiData].
     * Framework-oriented.
     * Created by View, since string resources are tied to platform-specific
     * components (like Context), which should be avoided in ViewModels.
     */
    data class ViewData(
        val textField: TextFieldUiData.ViewData,
    )
}

fun ColorInputHexViewData(context: Context) =
    ColorInputHexUiData.ViewData(
        textField = TextFieldViewData(context),
    )

private fun TextFieldViewData(context: Context) =
    TextFieldUiData.ViewData(
        label = context.getString(R.string.color_input_hex_label),
        placeholder = context.getString(R.string.color_input_hex_placeholder),
        prefix = "#",
        trailingIcon = TextFieldUiData.ViewData.TrailingIcon.Exists(
            contentDesc = context.getString(R.string.color_input_hex_trailing_icon_desc),
        ),
    )