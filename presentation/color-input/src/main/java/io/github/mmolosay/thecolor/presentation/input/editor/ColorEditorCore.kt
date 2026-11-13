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
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexCore
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexOutcome
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.withColor
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbStateFactory
import io.github.mmolosay.thecolor.presentation.input.rgb.color
import io.github.mmolosay.thecolor.presentation.input.rgb.withColor
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorStateLenses as StateLenses

class ColorEditorCore @AssistedInject constructor(
    hexFactory: ColorInputHexCore.Factory,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val colorInputValidator: ColorInputValidator,
    private val colorComparator: ColorComparator,
    // TODO: today's, until the inputs have cores of their own (steps 4 to 6)
    private val rgbStateFactory: ColorInputRgbStateFactory,
    private val hsvStateFactory: ColorInputHsvStateFactory,
) {

    val hexCore = hexFactory.create()

    fun initialState(color: Color?): ColorEditorState =
        ColorEditorState(
            hex = hexCore.initialState(color),
            rgb = rgbStateFactory.create(color),
            hsv = hsvStateFactory.create(color),
            color = color,
            version = 0,
        )

    // region Setters

    fun ColorEditorState.withColor(color: Color?): ColorEditorState =
        context(colorConverter, colorInputMapper, colorComparator) {
            this.copy(
                hex = with(hexCore) { hex.withColor(color) },
                rgb = this.rgb.withColor(color),
                hsv = this.hsv.withColor(color),
                color = color,
                version = this.version + 1,
            )
        }

    fun ColorEditorState.withSubmissionAccepted(): ColorEditorState =
        this.copy(
            hex = with(hexCore) { hex.withSubmissionAccepted() },
        )

    private fun <T> ColorEditorState.withInputColor(
        lens: Lens<ColorEditorState, T>,
        color: Color?,
    ): ColorEditorState {
        if (context(colorComparator) { color isSameAs this.color }) return this
        val edited = lens.get(this)
        val updated = this.withColor(color)
        return lens.set(updated, edited) // the edited input keeps what the user entered
    }

    // endregion

    // region Transitions

    fun ColorEditorState.onHex(
        outcome: ColorInputHexOutcome?,
    ): Change<ColorEditorState, ColorEditorOutcome> {
        val color = with(hexCore) { hex.color() }
        var state = this.withInputColor(StateLenses.hex, color)
        if (outcome != ColorInputHexOutcome.Submitted) return Change(state)
        if (color == null) {
            val hex = with(hexCore) { state.hex.withSubmissionRejected() }
            state = state.copy(hex = hex)
            return Change(state)
        }
        val outcome = ColorEditorOutcome.ColorSubmitted(color)
        return Change(state, outcome)
    }

    fun ColorEditorState.onRgb(): Change<ColorEditorState, Nothing> {
        val color = context(colorInputValidator) { this.rgb.color() }
        val state = this.withInputColor(StateLenses.rgb, color)
        return Change(state)
    }

    fun ColorEditorState.onHsv(): Change<ColorEditorState, Nothing> {
        val color = this.hsv.color
        val state = this.withInputColor(StateLenses.hsv, color)
        return Change(state)
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorEditorCore
    }
}