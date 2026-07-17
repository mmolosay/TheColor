package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldHandle
import kotlinx.coroutines.Job

interface ColorInputHexHandle {
    fun facade(data: ColorInputHexData): ColorInputHexFacade
}

data class ColorInputHexFacade(
    val textField: TextFieldFacade,
    val inputSubmissionResult: ColorInputSubmissionResult?,
    val execute: (ColorInputHexAction) -> Job,
)

fun ColorInputHexHandle(viewModel: ColorInputHexViewModel): ColorInputHexHandle =
    ColorInputHexHandleImpl(
        textFieldHandle = viewModel.textFieldHandle,
        execute = viewModel::execute,
    )

private class ColorInputHexHandleImpl(
    private val textFieldHandle: TextFieldHandle,
    private val execute: (ColorInputHexAction) -> Job,
) : ColorInputHexHandle {

    override fun facade(data: ColorInputHexData): ColorInputHexFacade =
        ColorInputHexFacade(
            textField = textFieldHandle.facade(data = data.textField),
            inputSubmissionResult = data.inputSubmissionResult,
            execute = execute,
        )
}