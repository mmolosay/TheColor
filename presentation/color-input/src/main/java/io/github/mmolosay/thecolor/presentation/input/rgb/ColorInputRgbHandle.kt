package io.github.mmolosay.thecolor.presentation.input.rgb

import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmissionResult
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldFacade
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldHandle
import kotlinx.coroutines.flow.StateFlow

interface ColorInputRgbHandle {
    val dataFlow: StateFlow<ColorInputRgbData>
    fun facade(data: ColorInputRgbData): ColorInputRgbFacade
}

data class ColorInputRgbFacade(
    val rTextField: TextFieldFacade,
    val gTextField: TextFieldFacade,
    val bTextField: TextFieldFacade,
    val inputSubmissionResult: ColorInputSubmissionResult?,
    val isSmartBackspaceEnabled: Boolean,
    val execute: ExecuteColorInputRgbAction,
)

fun ColorInputRgbHandle(viewModel: ColorInputRgbViewModel): ColorInputRgbHandle =
    ColorInputRgbHandleImpl(
        rTextFieldHandle = viewModel.rTextFieldHandle,
        gTextFieldHandle = viewModel.gTextFieldHandle,
        bTextFieldHandle = viewModel.bTextFieldHandle,
        dataFlow = viewModel.dataFlow,
        execute = viewModel::execute,
    )

private class ColorInputRgbHandleImpl(
    private val rTextFieldHandle: TextFieldHandle,
    private val gTextFieldHandle: TextFieldHandle,
    private val bTextFieldHandle: TextFieldHandle,
    override val dataFlow: StateFlow<ColorInputRgbData>,
    private val execute: ExecuteColorInputRgbAction,
) : ColorInputRgbHandle {

    override fun facade(data: ColorInputRgbData): ColorInputRgbFacade =
        ColorInputRgbFacade(
            rTextField = rTextFieldHandle.facade(data.rTextField),
            gTextField = gTextFieldHandle.facade(data.gTextField),
            bTextField = bTextFieldHandle.facade(data.bTextField),
            inputSubmissionResult = data.inputSubmissionResult,
            isSmartBackspaceEnabled = data.isSmartBackspaceEnabled,
            execute = execute,
        )
}