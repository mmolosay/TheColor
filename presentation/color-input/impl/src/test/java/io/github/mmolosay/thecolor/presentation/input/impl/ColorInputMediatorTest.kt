package io.github.mmolosay.thecolor.presentation.input.impl

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.presentation.input.api.ColorInput
import io.github.mmolosay.thecolor.presentation.input.api.ColorInputColorStore
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.shouldBe
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
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType

class ColorInputMediatorTest {

    @OptIn(ExperimentalCoroutinesApi::class)
    val testDispatcher = UnconfinedTestDispatcher()

    val colorInputMapper: ColorInputMapper = mockk()

    val colorConverter: ColorConverter = mockk()

    val colorInputFactory: ColorInputFactory = mockk {
        every { emptyHex() } returns ColorInput.Hex("mocked")
        every { emptyRgb() } returns ColorInput.Rgb("mocked", "mocked", "mocked")
    }

    val colorInputColorStore: ColorInputColorStore = mockk {
        coEvery { set(any()) } just runs
    }

    lateinit var sut: ColorInputMediator

    @Test
    fun `when SUT is initialized, then empty 'ColorInput's are emitted from flows` () =
        runTest(testDispatcher) {
        val emptyHexColorInput = ColorInput.Hex("empty")
        every { colorInputFactory.emptyHex() } returns emptyHexColorInput
        val emptyRgbColorInput = ColorInput.Rgb("", "", "")
        every { colorInputFactory.emptyRgb() } returns emptyRgbColorInput

        createSut()

        sut.hexColorInputFlow.first() shouldBe emptyHexColorInput
        sut.rgbColorInputFlow.first() shouldBe emptyRgbColorInput
    }

    @Test
    @OptIn(FlowPreview::class)
    fun `received color with HEX input type is not emitted from HEX flow`() =
        runTest(testDispatcher) {
            val sentColor = mockk<Color>(relaxed = true)
            createSut()

            sut.send(color = sentColor, from = DomainColorInputType.Hex)

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
            val rgb: Color.Rgb = mockk()
            val emittedRgbColorInput: ColorInput.Rgb = mockk()
            every { with(colorConverter) { sentColor.toRgb() } } returns rgb
            every { with(colorInputMapper) { rgb.toColorInput() } } returns emittedRgbColorInput
            createSut()

            sut.send(color = sentColor, from = DomainColorInputType.Hex)

            sut.rgbColorInputFlow.first() shouldBe emittedRgbColorInput
        }

    @Test
    fun `received 'null' color results in emission of empty color input`() =
        runTest(testDispatcher) {
            val emptyRgbColorInput: ColorInput.Rgb = mockk()
            every { colorInputFactory.emptyRgb() } returns emptyRgbColorInput
            createSut()

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
            val collected = mutableListOf<ColorInput.Rgb>()

            launch {
                sut.rgbColorInputFlow
                    .drop(1) // ignore initial color
                    .take(2)
                    .toList(collected)
            }

            sut.send(color = null, from = DomainColorInputType.Hex)
            sut.send(color = null, from = DomainColorInputType.Hex)

            collected shouldContainExactly listOf(emptyRgbColorInput, emptyRgbColorInput)
            verify(atLeast = 2) { colorInputFactory.emptyRgb() }
        }

    @Test
    fun `received 'null' color is sent to 'color store' as 'null'`() =
        runTest(testDispatcher) {
            createSut()

            sut.send(color = null, from = null)

            verify { colorInputColorStore.set(color = null) }
        }

    @Test
    fun `received 'not-null' color is sent to 'color store' as it is`() =
        runTest(testDispatcher) {
            val sentColor = mockk<Color>(relaxed = true)
            createSut()

            sut.send(color = sentColor, from = null)

            verify { colorInputColorStore.set(sentColor) }
        }

    fun createSut() =
        ColorInputMediator(
            colorInputMapper = colorInputMapper,
            colorConverter = colorConverter,
            colorInputFactory = colorInputFactory,
            colorInputColorStore = colorInputColorStore,
        ).also {
            sut = it
        }
}