package io.github.mmolosay.thecolor.presentation.input.group

import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexHandle
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacade
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvHandle
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbHandle
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType

interface ColorInputGroupHandle {
    val dataFlow: StateFlow<ColorInputGroupData>
    fun facade(data: ColorInputGroupData): ColorInputGroupFacade
}

data class ColorInputGroupFacade(
    val hex: ColorInputHexFacade,
    val rgb: ColorInputRgbFacade,
    val hsv: ColorInputHsvFacade,
    val selectedInputType: DomainColorInputType,
    val orderedInputTypes: List<DomainColorInputType>,
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
    private val hex: ColorInputHexHandle,
    private val rgb: ColorInputRgbHandle,
    private val hsv: ColorInputHsvHandle,
    override val dataFlow: StateFlow<ColorInputGroupData>,
    private val execute: ExecuteColorInputGroupAction,
) : ColorInputGroupHandle {

    override fun facade(data: ColorInputGroupData): ColorInputGroupFacade =
        ColorInputGroupFacade(
            hex = hex.facade(data.hex),
            rgb = rgb.facade(data.rgb),
            hsv = hsv.facade(data.hsv),
            selectedInputType = data.selectedInputType,
            orderedInputTypes = data.orderedInputTypes,
            execute = execute,
        )
}