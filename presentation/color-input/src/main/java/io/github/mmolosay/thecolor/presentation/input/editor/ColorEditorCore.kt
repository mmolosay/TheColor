package io.github.mmolosay.thecolor.presentation.input.editor

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorComparator
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.isSameAs
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexCore
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexOutcome
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvStateFactory
import io.github.mmolosay.thecolor.presentation.input.hsv.withColor
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbCore
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbOutcome
import io.github.mmolosay.thecolor.utils.Lens
import io.github.mmolosay.thecolor.presentation.input.editor.ColorEditorStateLenses as StateLenses

class ColorEditorCore @AssistedInject constructor(
    hexFactory: ColorInputHexCore.Factory,
    rgbFactory: ColorInputRgbCore.Factory,
    private val colorConverter: ColorConverter,
    private val colorComparator: ColorComparator,
    // TODO: temporary
    private val hsvStateFactory: ColorInputHsvStateFactory,
) {

    val hexCore = hexFactory.create()
    val rgbCore = rgbFactory.create()

    fun initialState(color: Color?): ColorEditorState =
        ColorEditorState(
            hex = hexCore.initialState(color),
            rgb = rgbCore.initialState(color),
            hsv = hsvStateFactory.create(color),
            color = color,
            version = 0,
        )

    // region Setters

    fun ColorEditorState.withColor(color: Color?): ColorEditorState =
        context(colorConverter, colorComparator) {
            this.copy(
                hex = with(hexCore) { hex.withColor(color) },
                rgb = with(rgbCore) { rgb.withColor(color) },
                hsv = this.hsv.withColor(color),
                color = color,
                version = this.version + 1,
            )
        }

    fun ColorEditorState.withSubmissionAccepted(): ColorEditorState =
        this.copy(
            hex = with(hexCore) { hex.withSubmissionAccepted() },
            rgb = with(rgbCore) { rgb.withSubmissionAccepted() },
        )

    private fun ColorEditorState.withSubmissionRejected(): ColorEditorState =
        this.copy(
            hex = with(hexCore) { hex.withSubmissionRejected() },
            rgb = with(rgbCore) { rgb.withSubmissionRejected() },
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
        val state = this.withInputColor(StateLenses.hex, color)
        return when (outcome) {
            null -> Change(state)
            ColorInputHexOutcome.Submitted -> state.onInputSubmitted(color)
        }
    }

    fun ColorEditorState.onRgb(
        outcome: ColorInputRgbOutcome?,
    ): Change<ColorEditorState, ColorEditorOutcome> {
        val color = with(rgbCore) { rgb.color() }
        val state = this.withInputColor(StateLenses.rgb, color)
        return when (outcome) {
            null -> Change(state)
            ColorInputRgbOutcome.Submitted -> state.onInputSubmitted(color)
        }
    }

    fun ColorEditorState.onHsv(): Change<ColorEditorState, Nothing> {
        val color = this.hsv.color
        val state = this.withInputColor(StateLenses.hsv, color)
        return Change(state)
    }

    private fun ColorEditorState.onInputSubmitted(
        color: Color?,
    ): Change<ColorEditorState, ColorEditorOutcome> {
        if (color != null) {
            val outcome = ColorEditorOutcome.ColorSubmitted(color)
            return Change(this, outcome)
        } else {
            val state = this.withSubmissionRejected()
            return Change(state)
        }
    }

    // endregion

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorEditorCore
    }
}