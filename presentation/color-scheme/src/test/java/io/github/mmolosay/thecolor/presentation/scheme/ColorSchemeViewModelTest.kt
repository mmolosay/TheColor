package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.ApplyChangesButton
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeData.SwatchCount
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.State
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import kotlinx.coroutines.flow.drop
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorSchemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val getInitialState: GetInitialStateUseCase = mockk()
    val getColorScheme: GetColorSchemeUseCase = mockk()
    val colorSchemeDataFactory: ColorSchemeDataFactory = mockk()

    lateinit var sut: ColorSchemeViewModel

    val ColorSchemeViewModel.data: ColorSchemeData
        get() {
            this.dataStateFlow.value should beOfType<State.Ready>() // // assertion for clear failure message
            return (this.dataStateFlow.value as State.Ready).data
        }

    @Test
    fun `initial data state is as provided`() {
        every { getInitialState() } returns State.Loading

        createSut()

        sut.dataStateFlow.value should beOfType<State.Loading>()
    }

    @Test
    fun `call to 'get color scheme' emits Loading state from flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val data: ColorSchemeData = mockk {
                every { selectedMode } returns Mode.Monochrome
                every { selectedSwatchCount } returns SwatchCount.Six
            }
            every { getInitialState() } returns State.Ready(data)
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = any(),
                )
            } returns mockk()
            createSut()

            // "then" block
            launch {
                sut.dataStateFlow
                    .drop(1) // replayed initial state
                    .first() should beOfType<State.Loading>()
            }

            // "when" block
            sut.getColorScheme(seed = mockk())
        }

    @Test
    fun `call to 'get color scheme' emits Ready state from flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialState() } returns State.Loading
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = any(),
                )
            } returns mockk()
            createSut()

            sut.getColorScheme(seed = mockk())

            sut.dataStateFlow.value should beOfType<State.Ready>()
        }

    @Test
    fun `selecting new mode updates selected mode`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialState() } returns State.Loading
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            val onModeSelectSlot = slot<(Mode) -> Unit>()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = capture(onModeSelectSlot),
                    onSwatchCountSelect = any(),
                )
            } answers {
                ColorSchemeData(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    onModeSelect = onModeSelectSlot.captured,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    onSwatchCountSelect = {},
                    applyChangesButton = ApplyChangesButton.Hidden,
                )
            }
            createSut()
            sut.getColorScheme(seed = mockk()) // from other tests we know that state will become Ready after this call

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.selectedMode shouldBe Mode.Analogic
        }

    @Test
    fun `selecting new mode that is different from the active mode makes 'apply changes' button visible`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialState() } returns State.Loading
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            val onModeSelectSlot = slot<(Mode) -> Unit>()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = capture(onModeSelectSlot),
                    onSwatchCountSelect = any(),
                )
            } answers {
                ColorSchemeData(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    onModeSelect = onModeSelectSlot.captured,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    onSwatchCountSelect = {},
                    applyChangesButton = ApplyChangesButton.Hidden,
                )
            }
            createSut()
            sut.getColorScheme(seed = mockk()) // from other tests we know that state will become Ready after this call

            sut.data.onModeSelect(Mode.Analogic)

            sut.data.applyChangesButton should beOfType<ApplyChangesButton.Visible>()
        }

    @Test
    fun `selecting new mode that is same as the active mode makes 'apply changes' button hidden`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialState() } returns State.Loading
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            val onModeSelectSlot = slot<(Mode) -> Unit>()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = capture(onModeSelectSlot),
                    onSwatchCountSelect = any(),
                )
            } answers {
                ColorSchemeData(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Analogic,
                    onModeSelect = onModeSelectSlot.captured,
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    onSwatchCountSelect = {},
                    applyChangesButton = ApplyChangesButton.Hidden,
                )
            }
            createSut()
            sut.getColorScheme(seed = mockk()) // from other tests we know that state will become Ready after this call

            sut.data.onModeSelect(Mode.Monochrome)

            sut.data.applyChangesButton should beOfType<ApplyChangesButton.Hidden>()
        }

    @Test
    fun `selecting new swatch count updates selected swatch count`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialState() } returns State.Loading
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            val onSwatchCountSelectSlot = slot<(SwatchCount) -> Unit>()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = capture(onSwatchCountSelectSlot),
                )
            } answers {
                ColorSchemeData(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    onModeSelect = {},
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    onSwatchCountSelect = onSwatchCountSelectSlot.captured,
                    applyChangesButton = ApplyChangesButton.Hidden,
                )
            }
            createSut()
            sut.getColorScheme(seed = mockk()) // from other tests we know that state will become Ready after this call

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.selectedSwatchCount shouldBe SwatchCount.Thirteen
        }

    @Test
    fun `selecting new swatch count that is different from the active swatch count makes 'apply changes' button visible`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialState() } returns State.Loading
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            val onSwatchCountSelectSlot = slot<(SwatchCount) -> Unit>()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = capture(onSwatchCountSelectSlot),
                )
            } answers {
                ColorSchemeData(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    onModeSelect = {},
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Six,
                    onSwatchCountSelect = onSwatchCountSelectSlot.captured,
                    applyChangesButton = ApplyChangesButton.Hidden,
                )
            }
            createSut()
            sut.getColorScheme(seed = mockk()) // from other tests we know that state will become Ready after this call

            sut.data.onSwatchCountSelect(SwatchCount.Thirteen)

            sut.data.applyChangesButton should beOfType<ApplyChangesButton.Visible>()
        }

    @Test
    fun `selecting new swatch count that is same as the active swatch count makes 'apply changes' button hidden`() =
        runTest(mainDispatcherRule.testDispatcher) {
            every { getInitialState() } returns State.Loading
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns mockk()
            val onSwatchCountSelectSlot = slot<(SwatchCount) -> Unit>()
            every {
                colorSchemeDataFactory.create(
                    scheme = any(),
                    config = any(),
                    onModeSelect = any(),
                    onSwatchCountSelect = capture(onSwatchCountSelectSlot),
                )
            } answers {
                ColorSchemeData(
                    swatches = listOf(),
                    activeMode = Mode.Monochrome,
                    selectedMode = Mode.Monochrome,
                    onModeSelect = {},
                    activeSwatchCount = SwatchCount.Six,
                    selectedSwatchCount = SwatchCount.Thirteen,
                    onSwatchCountSelect = onSwatchCountSelectSlot.captured,
                    applyChangesButton = ApplyChangesButton.Hidden,
                )
            }
            createSut()
            sut.getColorScheme(seed = mockk()) // from other tests we know that state will become Ready after this call

            sut.data.onSwatchCountSelect(SwatchCount.Six)

            sut.data.applyChangesButton should beOfType<ApplyChangesButton.Hidden>()
        }

    //    @Test
    fun `click on 'apply changes' emits Loading state from flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color: Color = Color.Hex(0x1A803F)
            val domainColorScheme = DomainColorScheme()
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns domainColorScheme
//            every { with(colorConverter) { any<Color>().toHex() } } returns Color.Hex(0x123456)
            createSut()

//            sut.getColorScheme(seed = color)

            sut.dataStateFlow.value should beOfType<State.Loading>()
        }

    fun createSut() =
        ColorSchemeViewModel(
            getInitialState = getInitialState,
            getColorScheme = getColorScheme,
            colorSchemeDataFactory = colorSchemeDataFactory,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    object NoopOnClickAction : () -> Unit {
        override fun invoke() {}
    }

    fun DomainColorScheme() =
        ColorScheme(
            swatchDetails = listOf(
                ColorDetails(
                    color = Color.Hex(0x1A803F),
                    hexValue = "#1A803F",
                    hexClean = "1A803F",
                    rgbFractionR = 0.101960786f,
                    rgbFractionG = 0.5019608f,
                    rgbFractionB = 0.24705882f,
                    rgbR = 26,
                    rgbG = 128,
                    rgbB = 63,
                    rgbValue = "rgb(26, 128, 63)",
                    hslFractionH = 0.39379084f,
                    hslFractionS = 0.66233766f,
                    hslFractionL = 0.3019608f,
                    hslH = 142,
                    hslS = 66,
                    hslL = 30,
                    hslValue = "hsl(142, 66%, 30%)",
                    hsvFractionH = 0.39379084f,
                    hsvFractionS = 0.796875f,
                    hsvFractionV = 0.5019608f,
                    hsvH = 142,
                    hsvS = 80,
                    hsvV = 50,
                    hsvValue = "hsv(142, 80%, 50%)",
                    xyzFractionX = 0.26614392f,
                    xyzFractionY = 0.39851686f,
                    xyzFractionZ = 0.29663098f,
                    xyzX = 27,
                    xyzY = 40,
                    xyzZ = 30,
                    xyzValue = "XYZ(27, 40, 30)",
                    cmykFractionC = 0.796875f,
                    cmykFractionM = 0.0f,
                    cmykFractionY = 0.5078125f,
                    cmykFractionK = 0.49803922f,
                    cmykC = 80,
                    cmykM = 0,
                    cmykY = 51,
                    cmykK = 50,
                    cmykValue = "cmyk(80, 0, 51, 50)",
                    name = "Jewel",
                    exact = Color.Hex(0x126B40),
                    exactNameHex = "#126B40",
                    isNameMatchExact = false,
                    exactNameHexDistance = 1366,
                    imageBareUrl = "https://www.thecolorapi.com/id?format=svg&named=false&hex=1A803F",
                    imageNamedUrl = "https://www.thecolorapi.com/id?format=svg&hex=1A803F",
                    contrastHex = "#ffffff",
                ),
                ColorDetails(
                    color = Color.Hex(0x12294E),
                    hexValue = "#12294E",
                    hexClean = "12294E",
                    rgbFractionR = 0.07058824f,
                    rgbFractionG = 0.16078432f,
                    rgbFractionB = 0.30588236f,
                    rgbR = 18,
                    rgbG = 41,
                    rgbB = 78,
                    rgbValue = "rgb(18, 41, 78)",
                    hslFractionH = 0.6027778f,
                    hslFractionS = 0.625f,
                    hslFractionL = 0.1882353f,
                    hslH = 217,
                    hslS = 63,
                    hslL = 19,
                    hslValue = "hsl(217, 63%, 19%)",
                    hsvFractionH = 0.6027778f,
                    hsvFractionS = 0.7692308f,
                    hsvFractionV = 0.30588236f,
                    hsvH = 217,
                    hsvS = 77,
                    hsvV = 31,
                    hsvValue = "hsv(217, 77%, 31%)",
                    xyzFractionX = 0.14181882f,
                    xyzFractionY = 0.15208471f,
                    xyzFractionZ = 0.31126902f,
                    xyzX = 14,
                    xyzY = 15,
                    xyzZ = 31,
                    xyzValue = "XYZ(14, 15, 31)",
                    cmykFractionC = 0.7692308f,
                    cmykFractionM = 0.47435898f,
                    cmykFractionY = 0.0f,
                    cmykFractionK = 0.69411767f,
                    cmykC = 77,
                    cmykM = 47,
                    cmykY = 0,
                    cmykK = 69,
                    cmykValue = "cmyk(77, 47, 0, 69)",
                    name = "Blue Zodiac",
                    exact = Color.Hex(0x13264D),
                    exactNameHex = "#13264D",
                    isNameMatchExact = false,
                    exactNameHexDistance = 79,
                    imageBareUrl = "https://www.thecolorapi.com/id?format=svg&named=false&hex=12294E",
                    imageNamedUrl = "https://www.thecolorapi.com/id?format=svg&hex=12294E",
                    contrastHex = "#ffffff",
                ),
            )
        )
}