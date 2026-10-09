package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color

interface ColorInputHsvHandle {
    fun facade(data: ColorInputHsvData): ColorInputHsvFacade
}

data class ColorInputHsvFacade(
    val color: Color.Hsv?,
    val execute: ExecuteColorInputHsvAction,
)

fun ColorInputHsvHandle(viewModel: ColorInputHsvViewModel): ColorInputHsvHandle =
    ColorInputHsvHandleImpl(
        execute = viewModel::execute,
    )

private class ColorInputHsvHandleImpl(
    private val execute: ExecuteColorInputHsvAction,
) : ColorInputHsvHandle {

    override fun facade(data: ColorInputHsvData): ColorInputHsvFacade =
        ColorInputHsvFacade(
            color = data.color,
            execute = execute,
        )
}