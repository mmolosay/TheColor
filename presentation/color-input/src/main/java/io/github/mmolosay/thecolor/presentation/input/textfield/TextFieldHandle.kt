package io.github.mmolosay.thecolor.presentation.input.textfield

import io.github.mmolosay.thecolor.presentation.input.model.WithSource
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldData.Text

interface TextFieldHandle {
    fun facade(data: TextFieldData): TextFieldFacade
}

data class TextFieldFacade(
    val text: WithSource<Text>,
    val shouldSelectAllTextOnFocus: Boolean,
    val isClearTextFeatureEnabled: Boolean,
    val inputProcessor: TextFieldInputProcessor,
    val execute: ExecuteTextFieldAction,
)

fun TextFieldHandle(viewModel: TextFieldViewModel): TextFieldHandle =
    TextFieldHandleImpl(
        inputProcessor = viewModel.inputProcessor,
        execute = viewModel::execute,
    )

private class TextFieldHandleImpl(
    private val inputProcessor: TextFieldInputProcessor,
    private val execute: ExecuteTextFieldAction,
) : TextFieldHandle {

    override fun facade(data: TextFieldData): TextFieldFacade =
        TextFieldFacade(
            text = data.text,
            shouldSelectAllTextOnFocus = data.shouldSelectAllTextOnFocus,
            isClearTextFeatureEnabled = data.isClearTextFeatureEnabled,
            inputProcessor = inputProcessor,
            execute = execute,
        )
}