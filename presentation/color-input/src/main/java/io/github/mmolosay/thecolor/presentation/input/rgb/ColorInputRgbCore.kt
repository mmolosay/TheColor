package io.github.mmolosay.thecolor.presentation.input.rgb

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.user.preferences.DefaultUserPreferences
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.model.answered
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFormatter
import io.github.mmolosay.thecolor.utils.composition.Change

class ColorInputRgbCore @AssistedInject constructor(
    textFieldFactory: TextFieldCore.Factory,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val colorInputValidator: ColorInputValidator,
) {

    val rCore: TextFieldCore = textFieldFactory.create(
        formatter = RgbComponentFormatter,
    )
    val gCore: TextFieldCore = textFieldFactory.create(
        formatter = RgbComponentFormatter,
    )
    val bCore: TextFieldCore = textFieldFactory.create(
        formatter = RgbComponentFormatter,
    )

    fun initialState(color: Color?): ColorInputRgbState {
        val input = color?.toColorInput()
        return ColorInputRgbState(
            r = rCore.initialState(text = input?.r.orEmpty()),
            g = gCore.initialState(text = input?.g.orEmpty()),
            b = bCore.initialState(text = input?.b.orEmpty()),
            submission = null,
            isSmartBackspaceEnabled = DefaultUserPreferences.SmartBackspace.enabled,
        )
    }

    // region Getters

    fun ColorInputRgbState.color(): Color? {
        val input = this.colorInput()
        val result = with(colorInputValidator) { input.validate() }
        return result.getColorOrNull()
    }

    fun ColorInputRgbState.version(): Int =
        (this.r.version + this.g.version + this.b.version)

    fun ColorInputRgbState.toViewData(): ColorInputRgbViewData =
        ColorInputRgbViewData(
            answeredSubmission = this.submission as? ColorInputSubmission.Answered,
            isSmartBackspaceEnabled = this.isSmartBackspaceEnabled,
        )

    private fun ColorInputRgbState.colorInput(): ColorInput.Rgb =
        ColorInput.Rgb(r = this.r.text, g = this.g.text, b = this.b.text)

    // endregion

    // region Setters

    fun ColorInputRgbState.withColor(color: Color?): ColorInputRgbState {
        val input = color?.toColorInput()
        return this.copy(
            r = with(rCore) { r.withText(input?.r.orEmpty()) },
            g = with(gCore) { g.withText(input?.g.orEmpty()) },
            b = with(bCore) { b.withText(input?.b.orEmpty()) },
        )
    }

    fun ColorInputRgbState.withSubmissionAccepted(): ColorInputRgbState {
        val answer = ColorInputSubmission.Answer.Accepted
        val submission = this.submission.answered(answer, expectedInput = this.colorInput())
        return this.copy(submission = submission)
    }

    fun ColorInputRgbState.withSubmissionRejected(): ColorInputRgbState {
        val answer = ColorInputSubmission.Answer.Rejected
        val submission = this.submission.answered(answer, expectedInput = this.colorInput())
        return this.copy(submission = submission)
    }

    // endregion

    // region Transitions

    fun ColorInputRgbState.submit(): Change<ColorInputRgbState, ColorInputRgbOutcome> {
        val submission = ColorInputSubmission.Pending(input = this.colorInput())
        val state = this.copy(submission = submission)
        return Change(state, ColorInputRgbOutcome.Submitted)
    }

    fun ColorInputRgbState.ackAnswer(
        expected: ColorInputSubmission.Answered,
    ): Change<ColorInputRgbState, ColorInputRgbOutcome> {
        if (this.submission != expected) return Change(this)
        val state = this.copy(submission = null)
        return Change(state)
    }

    fun ColorInputRgbState.applySmartBackspace(
        enabled: Boolean,
    ): Change<ColorInputRgbState, ColorInputRgbOutcome> {
        val state = this.copy(isSmartBackspaceEnabled = enabled)
        return Change(state)
    }

    // endregion

    private fun Color.toColorInput(): ColorInput.Rgb {
        val color = this
        val rgb = with(colorConverter) { color.toRgb() }
        return with(colorInputMapper) { rgb.toColorInput() }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorInputRgbCore
    }
}

private object RgbComponentFormatter : TextFormatter {
    override fun format(text: String): String =
        text
            .filter { it.isDigit() }
            .take(3) // rgb component can be up to 3 digits long
            .let { string ->
                if (string.isEmpty()) return@let ""
                val rgbComponentMinValue = Color.Rgb.ComponentRange.first
                val rgbComponentMaxValue = Color.Rgb.ComponentRange.last
                var int = string.toIntOrNull() ?: rgbComponentMinValue // remove leading zeros
                // reduce int from right until it's in range
                while (int > rgbComponentMaxValue) {
                    int /= 10
                }
                int.toString()
            }
}