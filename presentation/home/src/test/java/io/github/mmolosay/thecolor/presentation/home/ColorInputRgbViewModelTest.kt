package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbUiData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbViewData
import io.github.mmolosay.thecolor.presentation.home.input.rgb.ColorInputRgbViewModel
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.junit.Test

class ColorInputRgbViewModelTest {

    val rInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true)
    val gInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true)
    val bInputField: ColorInputFieldUiData.ViewData = mockk(relaxed = true)
    val viewData = ColorInputRgbViewData(rInputField, gInputField, bInputField)

    val sut = ColorInputRgbViewModel(viewData)

    val uiData: ColorInputRgbUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `input processing keeps only digits`() {
        val result = uiData.rInputField.processText("abc1def2ghi3")

        result shouldBe "123"
    }

    @Test
    fun `input processing keeps only first 3 characters`() {
        val result = uiData.rInputField.processText("1234567890")

        result shouldBe "123"
    }
}