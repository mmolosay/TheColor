package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.domain.color.Color
import io.github.mmolosay.thecolor.domain.color.ColorConverter
import io.github.mmolosay.thecolor.domain.color.ColorFactory
import io.github.mmolosay.thecolor.domain.color.prototype.ColorPrototypeValidator
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.ColorInputSubmission
import io.github.mmolosay.thecolor.presentation.input.model.isPending
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbCore
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbOutcome
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbState
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.kotest.assertions.withClue
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.MethodSource

class ColorInputRgbCoreTest {

    // real implementations: they're pure, and RGB's texts come from them
    val colorConverter = ColorConverter()
    val colorInputMapper = ColorInputMapper()

    val sut = ColorInputRgbCore(
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
    fun `given all three components hold a color, when 'color' is invoked, then it's that color`() {
        with(sut) {
            val state = initialState(Color.Rgb(r = 26, g = 128, b = 63))

            val color = state.color()

            color shouldBe Color.Rgb(r = 26, g = 128, b = 63)
        }
    }

    @Test
    fun `given a component is empty, when 'color' is invoked, then it's 'null'`() {
        with(sut) {
            val state = initialState(Color.Rgb(r = 26, g = 128, b = 63)).enterTextInG("")

            val color = state.color()

            color.shouldBeNull()
        }
    }

    @Test
    fun `when 'submit' is invoked, then 'submission' is pending and 'Submitted' is handed up`() {
        with(sut) {
            val state = initialState(Color.Rgb(r = 26, g = 128, b = 63))

            val submitted = state.submit()

            val expectedInput = ColorInput.Rgb(r = "26", g = "128", b = "63")
            submitted.state.submission.isPending(expectedInput) shouldBe true
            submitted.outcome shouldBe ColorInputRgbOutcome.Submitted
        }
    }

    @Test
    fun `given 'submission' is pending and a component has changed since, when 'withSubmissionAccepted' is invoked, then nothing changes`() {
        with(sut) {
            val state = initialState(Color.Rgb(r = 26, g = 128, b = 63))
                .submit().state
                .enterTextInG("12")

            val accepted = state.withSubmissionAccepted()

            accepted shouldBe state
        }
    }

    @Test
    fun `given 'submission' is answered, when 'ackAnswer' is invoked with it, then 'submission' is 'null'`() {
        with(sut) {
            val state = initialState(Color.Rgb(r = 26, g = 128, b = 63))
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
            val accepted = initialState(Color.Rgb(r = 26, g = 128, b = 63))
                .submit().state
                .withSubmissionAccepted()
            val outdated = accepted.submission.shouldBeInstanceOf<ColorInputSubmission.Answered>()
            val state = accepted.submit().state

            val acked = state.ackAnswer(expected = outdated)

            acked shouldBe Change(state)
        }
    }

    @ParameterizedTest
    @MethodSource("data")
    fun `'enterText' keeps a component from 0 to 255`(
        text: String,
        expectedText: String,
    ) {
        with(sut) {
            val state = initialState(color = null)

            // only one component is checked, because all three are formatted the same way
            val entered = state.enterTextInG(text)

            withClue("Text \"$text\" should be formatted to \"$expectedText\"") {
                entered.g.text shouldBe expectedText
            }
        }
    }

    fun ColorInputRgbState.enterTextInG(text: String): ColorInputRgbState {
        val g = this.g
        val newG = with(sut.gCore) {
            g.enterText(text, expected = g.text)
        }.state
        return this.copy(g = newG)
    }

    companion object {

        @JvmStatic
        fun data(): List<Array<Any>> = listOf(
            /* #0 */ TestCase(text = "abc1def2ghi3", expectedText = "123"),
            /* #1 */ TestCase(text = "1234567890", expectedText = "123"),
            /* #2 */ TestCase(text = "", expectedText = ""),
            /* #3 */ TestCase(text = "0", expectedText = "0"),
            /* #4 */ TestCase(text = "03", expectedText = "3"),
            /* #5 */ TestCase(text = "003", expectedText = "3"),
            /* #6 */ TestCase(text = "000", expectedText = "0"),
            /* #7 */ TestCase(text = "30", expectedText = "30"),
            /* #8 */ TestCase(text = "255", expectedText = "255"),
            /* #9 */ TestCase(text = "256", expectedText = "25"),
        )
            .map { it.asArrayOfAnys() }

        data class TestCase(
            val text: String,
            val expectedText: String,
        )

        fun TestCase.asArrayOfAnys(): Array<Any> =
            arrayOf(text, expectedText)
    }
}
