package io.github.mmolosay.thecolor.presentation.scheme

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.model.ColorScheme
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorSchemeUseCase
import io.github.mmolosay.thecolor.presentation.scheme.ColorSchemeViewModel.State
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test

class ColorSchemeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val getColorScheme: GetColorSchemeUseCase = mockk()
    val colorConverter: ColorConverter = mockk()

    lateinit var sut: ColorSchemeViewModel

    @Test
    fun `initial data state is Loading`() {
        createSut()

        sut.dataStateFlow.value should beOfType<State.Loading>()
    }

    @Test
    fun `call to 'get color scheme' emits Ready state from flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color = Color.Hex(0x1A803F)
            val domainColorScheme = DomainColorScheme()
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns domainColorScheme
            every { with(colorConverter) { any<Color>().toHex() } } returns color
            createSut()

            sut.getColorScheme(seed = color)

            sut.dataStateFlow.value should beOfType<State.Ready>()
        }

    //    @Test
    fun `click on 'apply changes' emits Loading state from flow`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val color: Color = Color.Hex(0x1A803F)
            val domainColorScheme = DomainColorScheme()
            coEvery { getColorScheme(request = any<GetColorSchemeUseCase.Request>()) } returns domainColorScheme
            every { with(colorConverter) { any<Color>().toHex() } } returns Color.Hex(0x123456)
            createSut()

//            sut.getColorScheme(seed = color)

            sut.dataStateFlow.value should beOfType<State.Loading>()
        }

    fun createSut() =
        ColorSchemeViewModel(
            getColorScheme = getColorScheme,
            colorConverter = colorConverter,
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