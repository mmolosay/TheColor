package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
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

    val colorInputMapper: ColorInputMapper = mockk()

    val colorFactory: ColorFactory = mockk {
        every { from(any<ColorPrototype>()) } returns null
    }

    val colorConverter: ColorConverter = mockk {
        every { any<Color.Abstract>().toHex() } returns Color.Hex(0)
        every { any<Color.Abstract>().toRgb() } returns Color.Rgb(0, 0, 0)
    }

    lateinit var sut: ColorInputMediator

    @Test
    fun `initial not-null color from use case is emitted from flows`() = runTest(testDispatcher) {
        val initialColor = newAbstractColor()
        val hexColor = Color.Hex(0x00bfff)
        val rgbColor = Color.Rgb(0, 191, 255)
        val hexInput = ColorInput.Hex("00BFFF")
        val rgbInput = ColorInput.Rgb("0", "191", "255")
        coEvery { getInitialColor() } returns initialColor
        every { with(colorConverter) { initialColor.toHex() } } returns hexColor
        every { with(colorConverter) { initialColor.toRgb() } } returns rgbColor
        every { with(colorInputMapper) { hexColor.toColorInput() } } returns hexInput
        every { with(colorInputMapper) { rgbColor.toColorInput() } } returns rgbInput

        createSut()

        sut.hexColorInputFlow.first() shouldBe hexInput
        sut.rgbColorInputFlow.first() shouldBe rgbInput
    }

    @Test
    fun `initial null color from use case is emitted from flows`() = runTest(testDispatcher) {
        coEvery { getInitialColor() } returns null

        createSut()

        sut.hexColorInputFlow.first() shouldBe ColorInput.Hex("")
        sut.rgbColorInputFlow.first() shouldBe ColorInput.Rgb("", "", "")
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
    fun `received HEX color input is emitted from flows other than HEX`() =
        runTest(testDispatcher) {
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
            colorInputMapper = colorInputMapper,
            colorFactory = colorFactory,
            colorConverter = colorConverter,
        ).also {
            sut = it
        }

    fun newAbstractColor(): Color.Abstract =
        mockk(relaxed = true)
}