package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetInitialColorUseCase
import io.github.mmolosay.thecolor.presentation.ColorInputColorStore
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInputMediator.InputType
import io.github.thecolor.presentation.input.api.ColorInput
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
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
            val initialColor = mockAbstractColor()
            val abstractInitialColor = mockAbstractColor()
            coEvery { getInitialColor() } returns initialColor
            every { with(colorConverter) { initialColor.toAbstract() } } returns abstractInitialColor
            val hexColor = Color.Hex(0x00bfff)
            val hexInput = ColorInput.Hex("00BFFF")
            every { with(colorConverter) { abstractInitialColor.toHex() } } returns hexColor
            every { with(colorInputMapper) { hexColor.toColorInput() } } returns hexInput
            val rgbColor = Color.Rgb(0, 191, 255)
            val rgbInput = ColorInput.Rgb("0", "191", "255")
            every { with(colorConverter) { abstractInitialColor.toRgb() } } returns rgbColor
            every { with(colorInputMapper) { rgbColor.toColorInput() } } returns rgbInput
            createSut()

            sut.init()

            // value check doesn't matter here: if flow doesn't emmit, never finished
            // supsending call will cause the test to timeout and fail
            sut.hexColorInputFlow.first() shouldNotBe null
            sut.rgbColorInputFlow.first() shouldNotBe null
        }

    @Test
    fun `SUT updates 'color store' with initial color on initialization`() =
        runTest(testDispatcher) {
            val initialColor = mockAbstractColor()
            coEvery { getInitialColor() } returns initialColor
            val abstractInitialColor = mockAbstractColor()
            every { with(colorConverter) { initialColor.toAbstract() } } returns abstractInitialColor
            val hexColor = Color.Hex(0x00bfff)
            val hexInput = ColorInput.Hex("00BFFF")
            every { with(colorConverter) { abstractInitialColor.toHex() } } returns hexColor
            every { with(colorInputMapper) { hexColor.toColorInput() } } returns hexInput
            val rgbColor = Color.Rgb(0, 191, 255)
            val rgbInput = ColorInput.Rgb("0", "191", "255")
            every { with(colorConverter) { abstractInitialColor.toRgb() } } returns rgbColor
            every { with(colorInputMapper) { rgbColor.toColorInput() } } returns rgbInput
            createSut()

            sut.init()

            coVerify {
                colorInputColorStore.updateWith(color = initialColor)
            }
        }

    @Test
    fun `initial 'not-null' color is emitted from all flows`() = runTest(testDispatcher) {
        val initialColor = mockAbstractColor()
        coEvery { getInitialColor() } returns initialColor
        val abstractInitialColor = mockAbstractColor()
        every { with(colorConverter) { initialColor.toAbstract() } } returns abstractInitialColor
        val hexColor = Color.Hex(0x00bfff)
        val hexInput = ColorInput.Hex("00BFFF")
        every { with(colorConverter) { abstractInitialColor.toHex() } } returns hexColor
        every { with(colorInputMapper) { hexColor.toColorInput() } } returns hexInput
        val rgbColor = Color.Rgb(0, 191, 255)
        val rgbInput = ColorInput.Rgb("0", "191", "255")
        every { with(colorConverter) { abstractInitialColor.toRgb() } } returns rgbColor
        every { with(colorInputMapper) { rgbColor.toColorInput() } } returns rgbInput
        createSut()

        sut.init()

        sut.hexColorInputFlow.first() shouldBe hexInput
        sut.rgbColorInputFlow.first() shouldBe rgbInput
    }

    @Test
    fun `initial 'null' color is emitted from all flows`() = runTest(testDispatcher) {
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
    fun `received color with HEX input type is not emitted from HEX flow`() =
        runTest(testDispatcher) {
            val sentColor = mockk<Color>(relaxed = true)
            every { with(colorConverter) { sentColor.toAbstract() } } returns mockAbstractColor()
            createSut()
            sut.init()

            sut.send(color = sentColor, from = InputType.Hex)

            runBlocking {
                shouldThrow<TimeoutCancellationException> {
                    sut.hexColorInputFlow
                        .timeout(100.milliseconds)
                        .collect()
                }
            }
        }

    @Test
    fun `received color with HEX input type is emitted from flows other than HEX`() =
        runTest(testDispatcher) {
            val sentColor = mockk<Color>(relaxed = true)
            val sentAbstractColor = mockAbstractColor()
            val rgb: Color.Rgb = mockk()
            val emittedRgbColorInput: ColorInput.Rgb = mockk()
            every { with(colorConverter) { sentColor.toAbstract() } } returns sentAbstractColor
            every { with(colorConverter) { sentAbstractColor.toRgb() } } returns rgb
            every { with(colorInputMapper) { rgb.toColorInput() } } returns emittedRgbColorInput
            createSut()
            sut.init()

            sut.send(color = sentColor, from = InputType.Hex)

            sut.rgbColorInputFlow.first() shouldBe emittedRgbColorInput
        }

    @Test
    fun `received 'null' color results in emission of empty color input`() =
        runTest(testDispatcher) {
            val emptyRgbColorInput: ColorInput.Rgb = mockk()
            every { colorInputFactory.emptyRgb() } returns emptyRgbColorInput
            createSut()
            sut.init()

            sut.send(color = null, from = null)

            sut.rgbColorInputFlow.first() shouldBe emptyRgbColorInput
            // verification should go after the flow gains first collector and starts emitting
            verify { colorInputFactory.emptyRgb() }
        }

    @Test
    fun `receiving two consecutive 'null' colors results in two emissions of empty color inputs`() =
        runTest(testDispatcher) {
            val emptyRgbColorInput: ColorInput.Rgb = mockk()
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

            sut.send(color = null, from = InputType.Hex)
            sut.send(color = null, from = InputType.Hex)

            collected shouldContainExactly listOf(emptyRgbColorInput, emptyRgbColorInput)
            verify(atLeast = 2) { colorInputFactory.emptyRgb() }
        }

    @Test
    fun `received 'null' color is sent to 'color store' as 'null'`() =
        runTest(testDispatcher) {
            createSut()
            sut.init()

            sut.send(color = null, from = null)

            verify { colorInputColorStore.updateWith(color = null) }
        }

    @Test
    fun `received 'not-null' color is sent to 'color store' as it is`() =
        runTest(testDispatcher) {
            val sentColor = mockk<Color>(relaxed = true)
            every { with(colorConverter) { sentColor.toAbstract() } } returns mockAbstractColor()
            createSut()
            sut.init()

            sut.send(color = sentColor, from = null)

            verify { colorInputColorStore.updateWith(sentColor) }
        }

    fun createSut() =
        ColorInputMediator(
            getInitialColor = getInitialColor,
            colorInputMapper = colorInputMapper,
            colorConverter = colorConverter,
            colorInputFactory = colorInputFactory,
            colorInputColorStore = colorInputColorStore,
        ).also {
            sut = it
        }

    fun mockAbstractColor(): Color.Abstract =
        mockk(relaxed = true)
}