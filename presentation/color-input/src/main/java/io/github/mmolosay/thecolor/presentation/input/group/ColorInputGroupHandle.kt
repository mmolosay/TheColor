package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexHandle
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvHandle
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbHandle
import kotlinx.coroutines.flow.StateFlow

interface ColorInputGroupHandle {
    val hex: ColorInputHexHandle
    val rgb: ColorInputRgbHandle
    val hsv: ColorInputHsvHandle
    val dataFlow: StateFlow<ColorInputGroupData>
    fun facade(data: ColorInputGroupData): ColorInputGroupFacade
}

data class ColorInputGroupFacade(
    val data: ColorInputGroupData,
    val execute: ExecuteColorInputGroupAction,
)

fun ColorInputGroupHandle(viewModel: ColorInputGroupViewModel): ColorInputGroupHandle =
    ColorInputGroupHandleImpl(
        hex = viewModel.hexHandle,
        rgb = viewModel.rgbHandle,
        hsv = viewModel.hsvHandle,
        dataFlow = viewModel.dataFlow,
        execute = viewModel::execute,
    )

private class ColorInputGroupHandleImpl(
    override val hex: ColorInputHexHandle,
    override val rgb: ColorInputRgbHandle,
    override val hsv: ColorInputHsvHandle,
    override val dataFlow: StateFlow<ColorInputGroupData>,
    private val execute: ExecuteColorInputGroupAction,
) : ColorInputGroupHandle {

    override fun facade(data: ColorInputGroupData): ColorInputGroupFacade =
        ColorInputGroupFacade(
            data = data,
            execute = execute,
        )
}