package io.github.mmolosay.thecolor.presentation.input.editor

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.isSameAs
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexStateFactory
import io.github.mmolosay.thecolor.presentation.input.hex.color
import io.github.mmolosay.thecolor.presentation.input.hex.withColor
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.withColor
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.color
import io.github.mmolosay.thecolor.presentation.input.rgb.withColor
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorStateLenses as StateLenses

class ColorEditorCore @AssistedInject constructor(
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val colorInputValidator: ColorInputValidator,
    private val colorComparator: ColorComparator,
    // TODO: today's, until the inputs have cores of their own (steps 4 to 6)
    private val hexStateFactory: ColorInputHexStateFactory,
    private val rgbStateFactory: ColorInputRgbStateFactory,
    private val hsvStateFactory: ColorInputHsvStateFactory,
) {

    fun initialState(color: Color?): ColorEditorState =
        ColorEditorState(
            hex = hexStateFactory.create(color),
            rgb = rgbStateFactory.create(color),
            hsv = hsvStateFactory.create(color),
            colorState = ColorState(
                color = color,
                source = null,
                revision = ColorState.Revision(0),
            ),
        )

    // region Getters

    fun ColorEditorState.color(): Color? =
        this.colorState.color

    // endregion

    // region Setters

    fun ColorEditorState.withColor(
        color: Color?,
        source: DomainColorInputType?,
    ): ColorEditorState =
        context(colorConverter, colorInputMapper, colorComparator) {
            this.copy(
                hex = this.hex.withColor(color),
                rgb = this.rgb.withColor(color),
                hsv = this.hsv.withColor(color),
                colorState = ColorState(
                    color = color,
                    source = source,
                    revision = this.colorState.revision.next(),
                ),
            )
        }

    private fun <T> ColorEditorState.withInputColor(
        lens: Lens<ColorEditorState, T>,
        color: Color?,
        source: DomainColorInputType,
    ): ColorEditorState {
        if (context(colorComparator) { color isSameAs this.color() }) return this
        val edited = lens.get(this)
        val updated = this.withColor(color, source)
        return lens.set(updated, edited) // the edited input keeps what the user entered
    }

    // endregion

    // region Transitions

    fun ColorEditorState.onHex(): Change<ColorEditorState, Nothing> {
        val color = context(colorInputValidator) { this.hex.color() }
        return Change(this.withInputColor(StateLenses.hex, color, DomainColorInputType.Hex))
    }

    fun ColorEditorState.onRgb(): Change<ColorEditorState, Nothing> {
        val color = context(colorInputValidator) { this.rgb.color() }
        return Change(this.withInputColor(StateLenses.rgb, color, DomainColorInputType.Rgb))
    }

    fun ColorEditorState.onHsv(): Change<ColorEditorState, Nothing> {
        val color = this.hsv.color
        return Change(this.withInputColor(StateLenses.hsv, color, DomainColorInputType.Hsv))
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorEditorCore
    }
}