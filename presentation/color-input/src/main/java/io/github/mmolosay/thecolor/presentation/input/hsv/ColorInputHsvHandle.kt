package io.github.mmolosay.thecolor.presentation.input.hsv

import io.github.mmolosay.thecolor.domain.color.Color
import kotlinx.coroutines.Job

interface ColorInputHsvHandle {
    fun facade(data: ColorInputHsvData): ColorInputHsvFacade
}

data class ColorInputHsvFacade(
    val color: Color.Hsv?,
    val execute: (ColorInputHsvAction) -> Job,
)

fun ColorInputHsvHandle(viewModel: ColorInputHsvViewModel): ColorInputHsvHandle =
    ColorInputHsvHandleImpl(
        execute = viewModel::execute,
    )

private class ColorInputHsvHandleImpl(
    private val execute: (ColorInputHsvAction) -> Job,
) : ColorInputHsvHandle {

    override fun facade(data: ColorInputHsvData): ColorInputHsvFacade =
        ColorInputHsvFacade(
            color = data.color,
            execute = execute,
        )
}