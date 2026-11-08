package io.github.mmolosay.thecolor.presentation.input.textfield

import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text

interface TextFieldHandle {
    fun facade(data: TextFieldData): TextFieldFacade
}

data class TextFieldFacade(
    val text: Text,
    val shouldSelectAllTextOnFocus: Boolean,
    val isClearTextFeatureEnabled: Boolean,
    val inputProcessor: TextFieldInputProcessor,
    val execute: ExecuteTextFieldAction,
)

fun TextFieldHandle(
    inputProcessor: TextFieldInputProcessor,
    execute: ExecuteTextFieldAction,
): TextFieldHandle =
    TextFieldHandleImpl(
        inputProcessor = inputProcessor,
        execute = execute,
    )

private class TextFieldHandleImpl(
    private val inputProcessor: TextFieldInputProcessor,
    private val execute: ExecuteTextFieldAction,
) : TextFieldHandle {

    override fun facade(data: TextFieldData): TextFieldFacade =
        TextFieldFacade(
            text = data.text.data,
            shouldSelectAllTextOnFocus = data.shouldSelectAllTextOnFocus,
            isClearTextFeatureEnabled = data.isClearTextFeatureEnabled,
            inputProcessor = inputProcessor,
            execute = execute,
        )
}