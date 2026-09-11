package io.github.mmolosay.thecolor.presentation.input.hex

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldHandle
import kotlinx.coroutines.flow.StateFlow

interface ColorInputHexHandle {
    val dataFlow: StateFlow<ColorInputHexData>
    fun facade(data: ColorInputHexData): ColorInputHexFacade
}

data class ColorInputHexFacade(
    val textField: TextFieldFacade,
    val inputSubmissionResult: ColorInputSubmissionResult?,
    val execute: ExecuteColorInputHexAction,
)

fun ColorInputHexHandle(viewModel: ColorInputHexViewModel): ColorInputHexHandle =
    ColorInputHexHandleImpl(
        textFieldHandle = viewModel.textFieldHandle,
        dataFlow = viewModel.dataFlow,
        execute = viewModel::execute,
    )

private class ColorInputHexHandleImpl(
    private val textFieldHandle: TextFieldHandle,
    override val dataFlow: StateFlow<ColorInputHexData>,
    private val execute: ExecuteColorInputHexAction,
) : ColorInputHexHandle {

    override fun facade(data: ColorInputHexData): ColorInputHexFacade =
        ColorInputHexFacade(
            textField = textFieldHandle.facade(data = data.textField),
            inputSubmissionResult = data.inputSubmissionResult,
            execute = execute,
        )
}