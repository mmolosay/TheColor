package io.github.mmolosay.thecolor.presentation.input.editor

import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFacade
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexHandle
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFacade
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvHandle
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFacade
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbHandle
import kotlinx.coroutines.flow.StateFlow

interface ColorEditorHandle {
    val dataFlow: StateFlow<ColorEditorData>
    fun facade(data: ColorEditorData): ColorEditorFacade
}

data class ColorEditorFacade(
    val hex: ColorInputHexFacade,
    val rgb: ColorInputRgbFacade,
    val hsv: ColorInputHsvFacade,
)

fun ColorEditorHandle(
    editor: ColorEditor,
    hex: ColorInputHexHandle,
    rgb: ColorInputRgbHandle,
    hsv: ColorInputHsvHandle,
): ColorEditorHandle =
    ColorEditorHandleImpl(
        dataFlow = editor.dataFlow,
        hex = hex,
        rgb = rgb,
        hsv = hsv,
    )

private class ColorEditorHandleImpl(
    override val dataFlow: StateFlow<ColorEditorData>,
    private val hex: ColorInputHexHandle,
    private val rgb: ColorInputRgbHandle,
    private val hsv: ColorInputHsvHandle,
) : ColorEditorHandle {

    override fun facade(data: ColorEditorData): ColorEditorFacade =
        ColorEditorFacade(
            hex = hex.facade(data.hex),
            rgb = rgb.facade(data.rgb),
            hsv = hsv.facade(data.hsv),
        )
}