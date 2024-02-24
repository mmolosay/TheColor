package io.github.mmolosay.thecolor.presentation.input

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.model.ColorInput
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.runs
import io.mockk.verify
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

    val colorInputToAbstract: ColorInputToAbstractColorUseCase = mockk()

    val colorInputMapper: ColorInputMapper = mockk()

    val colorConverter: ColorConverter = mockk()

    val colorInputFactory: ColorInputFactory = mockk {
        every { emptyHex() } returns ColorInput.Hex("mocked")
        every { emptyRgb() } returns ColorInput.Rgb("mocked", "mocked", "mocked")
    }

    val colorInputColorStore: ColorInputColorStore = mockk {
        coEvery { updateWith(any()) } just runs
    }

    lateinit var sut: ColorInputMediator

    @OptIn(FlowPreview::class)
    @Test
    fun `SUT emits no values from flows when created and not initialized`() {
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
    fun `SUT emits from flows when initialized and initial color is not 'null'`() =
        runTest(testDispatcher) {
            val initialColor = newAbstractColor()
            val hexColor = Color.Hex(0x00bfff)
            val rgbColor = Color.Rgb(0, 191, 255)
            val hexInput = ColorInput.Hex("00BFFF")
            val rgbInput = ColorInput.Rgb("0", "191", "255")
            coEvery { getInitialColor() } returns initialColor
            every { with(colorConverter) { initialColor.toHex() } } returns hexColor
            every { with(colorInputMapper) { hexColor.toColorInput() } } returns hexInput
            every { with(colorConverter) { initialColor.toRgb() } } returns rgbColor
            every { with(colorInputMapper) { rgbColor.toColorInput() } } returns rgbInput
            createSut()

            sut.init()

            // value check doesn't matter here: if flow doesn't emmit, never finished
            // supsending call will cause the test to timeout and failure
            sut.hexColorInputFlow.first() shouldNotBe null
            sut.rgbColorInputFlow.first() shouldNotBe null
        }

    @Test
    fun `initial 'not-null' color from use case is emitted from flows`() = runTest(testDispatcher) {
        val initialColor = newAbstractColor()
        val hexColor = Color.Hex(0x00bfff)
        val rgbColor = Color.Rgb(0, 191, 255)
        val hexInput = ColorInput.Hex("00BFFF")
        val rgbInput = ColorInput.Rgb("0", "191", "255")
        coEvery { getInitialColor() } returns initialColor
        every { with(colorConverter) { initialColor.toHex() } } returns hexColor
        every { with(colorInputMapper) { hexColor.toColorInput() } } returns hexInput
        every { with(colorConverter) { initialColor.toRgb() } } returns rgbColor
        every { with(colorInputMapper) { rgbColor.toColorInput() } } returns rgbInput

        createSut()
        sut.init()

        sut.hexColorInputFlow.first() shouldBe hexInput
        sut.rgbColorInputFlow.first() shouldBe rgbInput
    }

    @Test
    fun `initial 'null' color from use case is emitted from flows`() = runTest(testDispatcher) {
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
    fun `received valid HEX color input is not emitted from HEX flow`() = runTest(testDispatcher) {
        val sentColorInput: ColorInput = mockk()
        every { with(colorInputToAbstract) { sentColorInput.toAbstractOrNull() } } returns newAbstractColor()
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
    fun `received valid HEX color input is emitted from flows other than HEX`() =
        runTest(testDispatcher) {
            val sentColorInput: ColorInput = mockk()
            val abstract = newAbstractColor()
            val rgb: Color.Rgb = mockk()
            val emittedRgbColorInput: ColorInput.Rgb = mockk()
            every { with(colorInputToAbstract) { sentColorInput.toAbstractOrNull() } } returns abstract
            every { with(colorConverter) { abstract.toRgb() } } returns rgb
            every { with(colorInputMapper) { rgb.toColorInput() } } returns emittedRgbColorInput
            createSut()
            sut.init()

            sut.send(sentColorInput)

            sut.rgbColorInputFlow.first() shouldBe emittedRgbColorInput
        }

    @Test
    fun `received invalid color input results in emission of empty color input`() =
        runTest(testDispatcher) {
            val sentColorInput: ColorInput = mockk()
            val emptyRgbColorInput: ColorInput.Rgb = mockk()
            every { with(colorInputToAbstract) { sentColorInput.toAbstractOrNull() } } returns null
            every { colorInputFactory.emptyRgb() } returns emptyRgbColorInput
            createSut()
            sut.init()

            sut.send(sentColorInput)

            sut.rgbColorInputFlow.first() shouldBe emptyRgbColorInput
            // verification should go after the flow gains first collector and starts emitting
            verify { colorInputFactory.emptyRgb() }
        }

    @Test
    fun `receiving two consecutive invalid color inputs results in two emissions of empty color inputs`() =
        runTest(testDispatcher) {
            val emptyRgbColorInput: ColorInput.Rgb = mockk()
            every { with(colorInputToAbstract) { any<ColorInput>().toAbstractOrNull() } } returns null
            every { colorInputFactory.emptyRgb() } returns emptyRgbColorInput
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

            collected shouldContainExactly listOf(emptyRgbColorInput, emptyRgbColorInput)
            verify(atLeast = 2) { colorInputFactory.emptyRgb() }
        }

    @Test
    fun `received invalid color input is sent to color input store as null`() =
        runTest(testDispatcher) {
            val sentColorInput: ColorInput = mockk()
            every { with(colorInputToAbstract) { sentColorInput.toAbstractOrNull() } } returns null
            createSut()
            sut.init()

            sut.send(sentColorInput)

            verify { colorInputColorStore.updateWith(null) }
        }

    @Test
    fun `received valid color input is sent to color input store as color`() =
        runTest(testDispatcher) {
            val sentColorInput: ColorInput = mockk()
            val abstract = newAbstractColor()
            every { with(colorInputToAbstract) { sentColorInput.toAbstractOrNull() } } returns abstract
            createSut()
            sut.init()

            sut.send(sentColorInput)

            verify { colorInputColorStore.updateWith(abstract) }
        }

    fun createSut() =
        ColorInputMediator(
            getInitialColor = getInitialColor,
            colorInputToAbstract = colorInputToAbstract,
            colorInputMapper = colorInputMapper,
            colorConverter = colorConverter,
            colorInputFactory = colorInputFactory,
            colorInputColorStore = colorInputColorStore,
        ).also {
            sut = it
        }

    fun newAbstractColor(): Color.Abstract =
        mockk(relaxed = true)
}