package io.github.mmolosay.thecolor.presentation.input.hex

import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.ColorInputMapper
import io.github.mmolosay.thecolor.presentation.input.ColorInputValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.model.answered
import io.github.mmolosay.thecolor.presentation.input.model.getColorOrNull
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFormatter

class ColorInputHexCore @AssistedInject constructor(
    textFieldFactory: TextFieldCore.Factory,
    private val colorConverter: ColorConverter,
    private val colorInputMapper: ColorInputMapper,
    private val colorInputValidator: ColorInputValidator,
) {

    val textFieldCore = textFieldFactory.create(
        formatter = HexFormatter,
    )

    fun initialState(color: Color?): ColorInputHexState {
        val text = color?.toColorInput()?.string.orEmpty()
        val textField = textFieldCore.initialState(text = text)
        return ColorInputHexState(textField = textField, submission = null)
    }

    // region Getters

    fun ColorInputHexState.color(): Color? {
        val colorInput = this.colorInput()
        val result = with(colorInputValidator) { colorInput.validate() }
        return result.getColorOrNull()
    }

    fun ColorInputHexState.toViewData(): ColorInputHexViewData =
        ColorInputHexViewData(
            answeredSubmission = this.submission as? ColorInputSubmission.Answered,
        )

    private fun ColorInputHexState.colorInput(): ColorInput.Hex =
        ColorInput.Hex(string = this.textField.text)

    // endregion

    // region Setters

    fun ColorInputHexState.withColor(color: Color?): ColorInputHexState {
        val text = color?.toColorInput()?.string.orEmpty()
        val textField = with(textFieldCore) { textField.withText(text) }
        if (textField == this.textField) return this
        return this.copy(textField = textField)
    }

    fun ColorInputHexState.withSubmissionAccepted(): ColorInputHexState {
        val answer = ColorInputSubmission.Answer.Accepted
        val submission = this.submission.answered(answer, expectedInput = this.colorInput())
        return this.copy(submission = submission)
    }

    fun ColorInputHexState.withSubmissionRejected(): ColorInputHexState {
        val answer = ColorInputSubmission.Answer.Rejected
        val submission = this.submission.answered(answer, expectedInput = this.colorInput())
        return this.copy(submission = submission)
    }

    // endregion

    // region Transitions

    fun ColorInputHexState.submit(): Change<ColorInputHexState, ColorInputHexOutcome> {
        val submission = ColorInputSubmission.Pending(input = this.colorInput())
        val state = this.copy(submission = submission)
        return Change(state, ColorInputHexOutcome.Submitted)
    }

    fun ColorInputHexState.ackAnswer(
        expected: ColorInputSubmission.Answered,
    ): Change<ColorInputHexState, ColorInputHexOutcome> {
        if (this.submission != expected) return Change(this)
        val state = this.copy(submission = null)
        return Change(state)
    }

    // endregion

    private fun Color.toColorInput(): ColorInput.Hex {
        val color = this
        val hex = with(colorConverter) { color.toHex() }
        return with(colorInputMapper) { hex.toColorInput() }
    }

    @AssistedFactory
    fun interface Factory {
        fun create(): ColorInputHexCore
    }
}

private object HexFormatter : TextFormatter {
    override fun format(text: String): String =
        text
            .uppercase()
            .filter { it.isDigit() || it in 'A'..'F' }
            .take(6) // HEX color can be up to 6 symbols long
}