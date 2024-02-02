package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorPrototype
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.ColorFactory
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.github.mmolosay.thecolor.presentation.input.model.isCompleteFromUserPerspective
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.timeout
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
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

    @OptIn(FlowPreview::class)
    @Test
    fun `sut emits no values from flows when created and not initialized`() {
        createSut()

        runBlocking {
            shouldThrow<TimeoutCancellationException> {
                sut.hexColorInputFlow
                    .timeout(100.milliseconds)
                    .collect()
            }
        }
    }

    @Test
    fun `sut emits from flows when initialized and initial color is not null`() =
        runTest(testDispatcher) {
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

            sut.init()

            // value check doesn't matter here: if flow doesn't emmit, never finished
            // supsending call will cause the test to timeout and failure
            sut.hexColorInputFlow.first() shouldNotBe null
            sut.rgbColorInputFlow.first() shouldNotBe null
        }

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
        sut.init()

        sut.hexColorInputFlow.first() shouldBe hexInput
        sut.rgbColorInputFlow.first() shouldBe rgbInput
    }

    @Test
    fun `initial null color from use case is emitted from flows`() = runTest(testDispatcher) {
        coEvery { getInitialColor() } returns null
        every { colorInputFactory.emptyHex() } returns ColorInput.Hex("empty")
        every { colorInputFactory.emptyRgb() } returns ColorInput.Rgb("em", "p", "ty")

        createSut()
        sut.init()

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
        sut.init()

        sut.send(sentColorInput)

        runBlocking {
            shouldThrow<TimeoutCancellationException> {
                sut.hexColorInputFlow
                    .timeout(100.milliseconds)
                    .collect()
            }
        }
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
            sut.init()

            sut.send(sentColorInput)

            sut.rgbColorInputFlow.first() shouldBe emittedRgbColorInput
        }

    @Test
    fun `incomplete color input results in emission of empty color input`() =
        runTest(testDispatcher) {
            val sentColorInput: ColorInput = ColorInput.Hex("gibberish")
            val emittedRgbColorInput = ColorInput.Rgb("", "", "")
            mockkStatic(ColorInput::isCompleteFromUserPerspective)
            every { sentColorInput.isCompleteFromUserPerspective() } returns false
            every { colorInputFactory.emptyRgb() } returns emittedRgbColorInput
            createSut()
            sut.init()

            sut.send(sentColorInput)

            sut.rgbColorInputFlow.first() shouldBe emittedRgbColorInput
            unmockkAll()
        }

    @Test
    fun `two consecutive incomplete color inputs result in two emissions of empty color inputs`() =
        runTest(testDispatcher) {
            val emittedRgbColorInput = ColorInput.Rgb("", "", "")
            mockkStatic(ColorInput::isCompleteFromUserPerspective)
            every { any<ColorInput>().isCompleteFromUserPerspective() } returns false
            every { colorInputFactory.emptyRgb() } returns emittedRgbColorInput
            createSut()
            sut.init()
            val collected = mutableListOf<ColorInput.Rgb>()

            launch {
                sut.rgbColorInputFlow
                    .drop(1) // ignore initial color
                    .take(2)
                    .toList(collected)
            }

            sut.send(ColorInput.Hex("gibberish-1"))
            sut.send(ColorInput.Hex("gibberish-2"))

            collected shouldContainExactly listOf(emittedRgbColorInput, emittedRgbColorInput)
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