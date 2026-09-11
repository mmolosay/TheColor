package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import kotlinx.coroutines.flow.StateFlow

interface ColorInputHsvHandle {
    val dataFlow: StateFlow<ColorInputHsvData>
    fun facade(data: ColorInputHsvData): ColorInputHsvFacade
}

data class ColorInputHsvFacade(
    val color: Color.Hsv?,
    val execute: ExecuteColorInputHsvAction,
)

fun ColorInputHsvHandle(viewModel: ColorInputHsvViewModel): ColorInputHsvHandle =
    ColorInputHsvHandleImpl(
        dataFlow = viewModel.dataFlow,
        execute = viewModel::execute,
    )

private class ColorInputHsvHandleImpl(
    override val dataFlow: StateFlow<ColorInputHsvData>,
    private val execute: ExecuteColorInputHsvAction,
) : ColorInputHsvHandle {

    override fun facade(data: ColorInputHsvData): ColorInputHsvFacade =
        ColorInputHsvFacade(
            color = data.color,
            execute = execute,
        )
}