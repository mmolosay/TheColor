package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexUiData
import io.github.mmolosay.thecolor.presentation.home.input.hex.ColorInputHexViewModel
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Test

class ColorInputHexViewModelTest {

    val viewData: ColorInputFieldUiData.ViewData = mockk(relaxed = true)

    val sut = ColorInputHexViewModel(viewData)

    val uiData: ColorInputHexUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `input processing keeps only digits and letter A-F`() {
        val result = uiData.inputField.processText("123abc_!.@ABG")

        result shouldBe "123AB"
    }

    @Test
    fun `input processing keeps only first 6 characters`() {
        val result = uiData.inputField.processText("123456789ABCDEF")

        result shouldBe "123456"
    }
}