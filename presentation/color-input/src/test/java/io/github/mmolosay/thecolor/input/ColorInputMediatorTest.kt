package io.github.mmolosay.thecolor.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import org.junit.Test
import kotlin.time.Duration.Companion.milliseconds

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

    val colorConverter: ColorConverter = mockk()

    val colorInputFactory: ColorInputFactory = mockk {
        every { emptyHex() } returns ColorInput.Hex("mocked")
        every { emptyRgb() } returns ColorInput.Rgb("mocked", "mocked", "mocked")
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
        every { colorInputFactory.emptyHex() } returns ColorInput.Hex("empty")
        every { colorInputFactory.emptyRgb() } returns ColorInput.Rgb("em", "p", "ty")

        createSut()

        sut.hexColorInputFlow.first() shouldBe ColorInput.Hex("empty")
        sut.rgbColorInputFlow.first() shouldBe ColorInput.Rgb("em", "p", "ty")
    }

    @Test
    @OptIn(FlowPreview::class)
    fun `received HEX color input is not emitted from HEX flow`() = runTest(testDispatcher) {
        val sentColorInput: ColorInput = ColorInput.Hex("00BFFF")
        val prototype: ColorPrototype = ColorPrototype.Hex(0x00bff)
        val colorFromFactory: Color = Color.Hex(0x00bff)
        val abstract = newAbstractColor()
        val hex = Color.Hex(0x00bff)
        every { with(colorInputMapper) { sentColorInput.toPrototype() } } returns prototype
        every { colorFactory.from(prototype) } returns colorFromFactory
        every { with(colorConverter) { colorFromFactory.toAbstract() } } returns abstract
        every { with(colorConverter) { abstract.toHex() } } returns hex
        createSut()

        launch {
            shouldThrow<TimeoutCancellationException> {
                sut.hexColorInputFlow
                    .drop(1)
                    .timeout(1000.milliseconds)
                    .first()
            }
        }

        sut.send(sentColorInput)
    }

    @Test
    fun `received HEX color input is emitted from flows other than HEX`() =
        runTest(testDispatcher) {
            val sentColorInput: ColorInput = ColorInput.Hex("00BFFF")
            val prototype: ColorPrototype = ColorPrototype.Hex(0x00bff)
            val colorFromFactory: Color = Color.Hex(0x00bff)
            val abstract = newAbstractColor()
            val rgb = Color.Rgb(0, 191, 255)
            val emittedRgbColorInput = ColorInput.Rgb("0", "191", "255")
            every { with(colorInputMapper) { sentColorInput.toPrototype() } } returns prototype
            every { colorFactory.from(prototype) } returns colorFromFactory
            every { with(colorConverter) { colorFromFactory.toAbstract() } } returns abstract
            every { with(colorConverter) { abstract.toRgb() } } returns rgb
            every { with(colorInputMapper) { rgb.toColorInput() } } returns emittedRgbColorInput
            createSut()

            val collectionJob = launch {
                sut.rgbColorInputFlow
                    .drop(1) // ignore initial color
                    .first() shouldBe emittedRgbColorInput
            }

            sut.send(sentColorInput)
            collectionJob.join()
        }

    @Test
    fun `incomplete color input results in emission of empty color input`() =
        runTest(testDispatcher) {
            // due to impl of StateFlow, we can't emit two equal values consecutively
            // se emitting some non-empty initial value first
            coEvery { getInitialColor() } returns newAbstractColor()
            every { with(colorConverter) { any<Color.Abstract>().toHex() } } returns mockk()
            every { with(colorConverter) { any<Color.Abstract>().toRgb() } } returns mockk()
            every { with(colorInputMapper) { any<Color.Hex>().toColorInput() } } returns mockk()
            every { with(colorInputMapper) { any<Color.Rgb>().toColorInput() } } returns mockk()

            val sentColorInput: ColorInput = ColorInput.Hex("gibberish")
            val emittedRgbColorInput = ColorInput.Rgb("", "", "")
            mockkStatic(ColorInput::isCompleteFromUserPerspective)
            every { sentColorInput.isCompleteFromUserPerspective() } returns false
            every { colorInputFactory.emptyRgb() } returns emittedRgbColorInput
            createSut()

            launch {
                sut.rgbColorInputFlow
                    .drop(1) // ignore initial color
                    .first() shouldBe emittedRgbColorInput
            }

            sut.send(sentColorInput)

            unmockkAll()
        }

    fun createSut() =
        ColorInputMediator(
            getInitialColor = getInitialColor,
            colorInputMapper = colorInputMapper,
            colorFactory = colorFactory,
            colorConverter = colorConverter,
            colorInputFactory = colorInputFactory,
        ).also {
            sut = it
        }

    fun newAbstractColor(): Color.Abstract =
        mockk(relaxed = true)
}