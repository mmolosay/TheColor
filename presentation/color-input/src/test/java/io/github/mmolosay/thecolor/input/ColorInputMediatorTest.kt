package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorPrototypeConverter
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import io.github.mmolosay.thecolor.presentation.color.ColorInput
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test

class ColorInputMediatorTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = UnconfinedTestDispatcher()

    val getInitialColor: GetInitialColorUseCase = mockk {
        coEvery { this@mockk.invoke() } returns null
    }

    val colorPrototypeConverter: ColorPrototypeConverter = mockk {
        every { any<ColorPrototype>().hint(Color::class).toColorOrNull() } returns null
    }

    val colorConverter: ColorConverter = mockk {
        every { any<Color.Abstract>().toHex() } returns Color.Hex(0)
        every { any<Color.Abstract>().toRgb() } returns Color.Rgb(0, 0, 0)
    }

    lateinit var sut: ColorInputMediator

    @Test
    fun `initial color from use case is emitted from flows`() = runTest(testDispatcher) {
        val initialColor = newAbstractColor()
        coEvery { getInitialColor() } returns initialColor
        every { with(colorConverter) { initialColor.toHex() } } returns Color.Hex(0x00bfff)
        every { with(colorConverter) { initialColor.toRgb() } } returns Color.Rgb(0, 191, 255)

        createSut()

        sut.hexColorInputFlow.first() shouldBe ColorInput.Hex("00BFFF")
        sut.rgbColorInputFlow.first() shouldBe ColorInput.Rgb("0", "191", "255")
    }

    @Test
    fun `received HEX color input is not emitted from HEX flow`() = runTest(testDispatcher) {
        createSut()
        var afterInitial: ColorInput.Hex? = null

        val collectionJob = launch {
            sut.hexColorInputFlow
                .drop(1) // ignore initial color
                .collect { afterInitial = it }
        }
        val colorInput = ColorInput.Hex("anything")
        sut.send(colorInput)

        afterInitial shouldBe null
        collectionJob.cancel()
    }

    @Test
    fun `received HEX color input is emitted from flows other than HEX`() = runTest(testDispatcher) {
        createSut()

        val collectionJob = launch {
            sut.rgbColorInputFlow
                .drop(1) // ignore initial color
                .first() shouldBe ColorInput.Rgb("", "", "") // empty from invalid
        }

        val colorInput = ColorInput.Hex("anything") // invalid
        sut.send(colorInput)
        collectionJob.cancel()
    }

    fun createSut() =
        ColorInputMediator(
            getInitialColor = getInitialColor,
            colorPrototypeConverter = colorPrototypeConverter,
            colorConverter = colorConverter,
        ).also {
            sut = it
        }

    fun newAbstractColor(): Color.Abstract =
        mockk(relaxed = true)
}