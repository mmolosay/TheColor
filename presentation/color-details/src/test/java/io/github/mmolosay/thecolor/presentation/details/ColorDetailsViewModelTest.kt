package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.domain.usecase.ColorConverter
import io.github.mmolosay.thecolor.domain.usecase.GetColorDetailsUseCase
import io.github.mmolosay.thecolor.domain.usecase.IsColorLightUseCase
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsData.ColorInt
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.State
import io.github.mmolosay.thecolor.testing.MainDispatcherRule
import io.kotest.matchers.should
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.beOfType
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.junit.Rule
import org.junit.Test

class ColorDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    val getColorDetails: GetColorDetailsUseCase = mockk()
    val colorConverter: ColorConverter = mockk()
    val isColorLight: IsColorLightUseCase = mockk()

    lateinit var sut: ColorDetailsViewModel

    @Test
    fun `call to 'get color details' emits Ready state from flow`() {
        val color = Color.Hex(0x1A803F)
        val domainDetails = ColorDetails()
        val hex = Color.Hex(0x1A803F)
        coEvery { getColorDetails.invoke(any<Color>()) } returns domainDetails
        every { with(colorConverter) { any<Color>().toHex() } } returns hex
        every { with(isColorLight) { any<Color>().isLight() } } returns false
        createSut()

        sut.getColorDetails(color)

        sut.dataStateFlow.value should beOfType<State.Ready>()
    }

    @Test
    fun `call to 'get color details' emits Ready state with correct data from flow`() {
        val color = Color.Hex(0x1A803F)
        val domainDetails = ColorDetails()
        coEvery { getColorDetails.invoke(any<Color>()) } returns domainDetails
        every { with(colorConverter) { Color.Hex(0x1A803F).toHex() } } returns Color.Hex(0x1A803F)
        every { with(colorConverter) { Color.Hex(0x126B40).toHex() } } returns Color.Hex(0x126B40)
        every { with(isColorLight) { any<Color>().isLight() } } returns false
        createSut()

        sut.getColorDetails(color)

        val data = (sut.dataStateFlow.value as State.Ready).data
        val comparableData = data.copyWithNoopLambdas()
        comparableData shouldBe ColorDetailsData(
            colorName = "Jewel",
            useLightContentColors = true,
            hex = ColorDetailsData.Hex(value = "#1A803F"),
            rgb = ColorDetailsData.Rgb(
                r = "26",
                g = "128",
                b = "63",
            ),
            hsl = ColorDetailsData.Hsl(
                h = "142",
                s = "66",
                l = "30",
            ),
            hsv = ColorDetailsData.Hsv(
                h = "142",
                s = "80",
                v = "50",
            ),
            cmyk = ColorDetailsData.Cmyk(
                c = "80",
                m = "0",
                y = "51",
                k = "50",
            ),
            exactMatch = ColorDetailsData.ExactMatch.No(
                exactValue = "#126B40",
                exactColor = ColorInt(0x126B40),
                onExactClick = NoopOnClickAction,
                deviation = "1366",
            ),
            onViewColorSchemeClick = NoopOnClickAction,
        )
    }

    fun createSut() =
        ColorDetailsViewModel(
            getColorDetails = getColorDetails,
            colorConverter = colorConverter,
            isColorLight = isColorLight,
            ioDispatcher = mainDispatcherRule.testDispatcher,
        ).also {
            sut = it
        }

    fun ColorDetails() =
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
        )

    // Arrow lenses cannot be used properly: copy {} doesn't seem to work with my Kotlin version
    // https://arrow-kt.io/learn/immutable-data/lens/#more-powerful-copy
    fun ColorDetailsData.copyWithNoopLambdas() =
        this.copy(
            exactMatch = exactMatch.run {
                when (this) {
                    is ColorDetailsData.ExactMatch.No -> this.copy(
                        onExactClick = NoopOnClickAction,
                    )
                    is ColorDetailsData.ExactMatch.Yes -> this
                }
            },
            onViewColorSchemeClick = NoopOnClickAction,
        )

    object NoopOnClickAction : () -> Unit {
        override fun invoke() {}
    }
}