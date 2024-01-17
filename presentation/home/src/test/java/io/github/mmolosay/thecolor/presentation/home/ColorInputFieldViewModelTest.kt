package io.github.mmolosay.thecolor.presentation.home

import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.TrailingButton
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldUiData.ViewData.TrailingIcon
import io.github.mmolosay.thecolor.presentation.home.input.ColorInputFieldViewModel
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ColorInputFieldViewModelTest {

    val viewData: ColorInputFieldUiData.ViewData = mockk(relaxed = true)

    lateinit var sut: ColorInputFieldViewModel

    val uiData: ColorInputFieldUiData
        get() = sut.uiDataFlow.value

    @Test
    fun `trailing button is visible when text is non-empty`() = runTest {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()

        uiData.onTextChange("non-empty text")

        uiData.trailingButton should beOfType<TrailingButton.Visible>()
    }

    @Test
    fun `trailing button is hidden when text is empty`() = runTest {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()

        uiData.onTextChange("")

        uiData.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `trailing button is hidden when there is no such`() = runTest {
        every { viewData.trailingIcon } returns TrailingIcon.None
        createSut()

        uiData.onTextChange("any text")

        uiData.trailingButton should beOfType<TrailingButton.Hidden>()
    }

    @Test
    fun `text is cleared on trailing button click`() = runTest {
        every { viewData.trailingIcon } returns mockk<TrailingIcon.Exists>(relaxed = true)
        createSut()

        uiData.onTextChange("any text")
        (uiData.trailingButton as TrailingButton.Visible).onClick()

        uiData.text shouldBe ""
    }

    fun createSut(
        processText: (String) -> String = noopProcessText,
    ) =
        ColorInputFieldViewModel(
            viewData = viewData,
            processText = processText,
        ).also { this.sut = it }

    val noopProcessText: (String) -> String =
        mockk lambda@{
            val slot = slot<String>()
            every { this@lambda.invoke(capture(slot)) } answers { slot.captured }
        }
}