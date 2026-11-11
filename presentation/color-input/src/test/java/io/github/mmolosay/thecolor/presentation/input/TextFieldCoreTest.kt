package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.composition.Change
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFieldCore
import io.github.mmolosay.thecolor.presentation.input.textfield.TextFormatter
import io.kotest.matchers.shouldBe
import org.junit.jupiter.api.Test

class TextFieldCoreTest {

    val sut = TextFieldCore(
        formatter = TextFormatter { text -> text.take(6) },
    )

    @Test
    fun `given 'text' is 'F0', when 'enterText' is invoked with 'F0A' expecting 'F0', then 'text' is 'F0A'`() {
        with(sut) {
            val state = initialState(text = "F0")

            val entered = state.enterText(text = "F0A", expected = "F0")

            entered.state.text shouldBe "F0A"
        }
    }

    @Test
    fun `given 'text' is 'FFFFFF', when 'enterText' is invoked with 'F0A' expecting 'F0', then nothing changes`() {
        with(sut) {
            val state = initialState(text = "FFFFFF")

            val entered = state.enterText(text = "F0A", expected = "F0")

            entered shouldBe Change(state)
        }
    }

    @Test
    fun `given 'text' is 'FFFFFF', when 'enterText' is invoked with a text that is formatted to 'FFFFFF', then 'version' stays`() {
        with(sut) {
            val state = initialState(text = "FFFFFF")

            val entered = state.enterText(text = "FFFFFF0", expected = "FFFFFF")

            entered.state.version shouldBe state.version
        }
    }
}
