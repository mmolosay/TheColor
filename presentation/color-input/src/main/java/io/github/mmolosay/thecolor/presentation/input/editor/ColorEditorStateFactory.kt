package io.github.mmolosay.thecolor.presentation.input.editor

import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import javax.inject.Inject

class ColorEditorStateFactory @Inject constructor(
    private val hexFactory: ColorInputHexStateFactory,
    private val rgbFactory: ColorInputRgbStateFactory,
    private val hsvFactory: ColorInputHsvStateFactory,
) {
    fun create(color: Color?): ColorEditorState =
        ColorEditorState(
            hex = hexFactory.create(color),
            rgb = rgbFactory.create(color),
            hsv = hsvFactory.create(color),
            colorState = ColorState(
                color = color,
                source = null,
                revision = ColorState.Revision(0),
            ),
        )
}