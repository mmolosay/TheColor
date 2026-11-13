package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexCore
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexOutcome
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexState
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.model.isPending
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test

class ColorInputHexCoreTest {

    // real implementations: they're pure, and HEX's text comes from them
    val colorConverter = ColorConverter()
    val colorInputMapper = ColorInputMapper()

    val sut = ColorInputHexCore(
        textFieldFactory = TextFieldCore.Factory { formatter ->
            TextFieldCore(formatter)
        },
        colorConverter = colorConverter,
        colorInputMapper = colorInputMapper,
        colorInputValidator = ColorInputValidator(
            colorInputMapper = colorInputMapper,
            colorFactory = ColorFactory(
                colorPrototypeValidator = ColorPrototypeValidator(),
            ),
        ),
    )

    @Test
    fun `when 'enterText' is invoked with lowercase and non-HEX characters, then 'text' keeps only the HEX digits, in uppercase`() {
        with(sut) {
            val state = initialState(color = null)

            val entered = state.enterText("1a-8g0")

            entered.textField.text shouldBe "1A80"
        }
    }

    @Test
    fun `when 'enterText' is invoked with seven characters, then 'text' keeps the first six`() {
        with(sut) {
            val state = initialState(color = null)

            val entered = state.enterText("1A803F0")

            entered.textField.text shouldBe "1A803F"
        }
    }

    @Test
    fun `when 'submit' is invoked, then 'submission' is pending and 'Submitted' is handed up`() {
        with(sut) {
            val state = initialState(Color.Hex(0x1A803F))

            val submitted = state.submit()

            val expectedInput = ColorInput.Hex(string = "1A803F")
            submitted.state.submission.isPending(expectedInput) shouldBe true
            submitted.outcome shouldBe ColorInputHexOutcome.Submitted
        }
    }

    @Test
    fun `given 'submission' is pending and the text has changed since, when 'withSubmissionAccepted' is invoked, then nothing changes`() {
        with(sut) {
            val state = initialState(Color.Hex(0x1A803F))
                .submit().state
                .enterText("1A803")

            val accepted = state.withSubmissionAccepted()

            accepted shouldBe state
        }
    }

    @Test
    fun `given 'submission' is answered, when 'ackAnswer' is invoked with it, then 'submission' is 'null'`() {
        with(sut) {
            val state = initialState(Color.Hex(0x1A803F))
                .submit().state
                .withSubmissionAccepted()
            val answered = state.submission.shouldBeInstanceOf<ColorInputSubmission.Answered>()

            val acked = state.ackAnswer(expected = answered)

            acked.state.submission.shouldBeNull()
        }
    }

    @Test
    fun `given 'submission' was answered and then submitted again, when 'ackAnswer' is invoked with the answered one, then nothing changes`() {
        with(sut) {
            val accepted = initialState(Color.Hex(0x1A803F))
                .submit().state
                .withSubmissionAccepted()
            val outdated = accepted.submission.shouldBeInstanceOf<ColorInputSubmission.Answered>()
            val state = accepted.submit().state

            val acked = state.ackAnswer(expected = outdated)

            acked shouldBe Change(state)
        }
    }

    fun ColorInputHexState.enterText(text: String): ColorInputHexState {
        val textField = this.textField
        val newTextField = with(sut.textFieldCore) {
            textField.enterText(text, expected = textField.text)
        }.state
        return this.copy(textField = newTextField)
    }
}
