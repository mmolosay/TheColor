package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.presentation.ColorInt
import io.kotest.matchers.shouldBe
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ColorDetailsUiDataFactoryTest(
    val data: ColorDetailsData,
    val viewData: ColorDetailsUiData.ViewData,
    val expectedUiData: ColorDetailsUiData,
) {

    @Test
    fun `combining ViewModel data and View data produces expected UI data`() {
        val uiData = ColorDetailsUiData(data, viewData)

        uiData shouldBe expectedUiData
    }

    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = with(TestParameters) {
            listOf(
                /* #0  */ params(data0(), viewData0(), uiData0()),
                /* #1  */ params(data1(), viewData1(), uiData1()),
            )
        }

        fun params(
            data: ColorDetailsData,
            viewData: ColorDetailsUiData.ViewData,
            expectedUiData: ColorDetailsUiData,
        ): Array<Any> =
            arrayOf(data, viewData, expectedUiData)
    }
}

private object TestParameters {

    // inline lambdas like '{}' aren't equal, so reusing the same instance
    val OnExactColorClick: () -> Unit = {}

    // region #0

    fun data0() = ColorDetailsData(
        colorName = "Jewel",
        hex = ColorDetailsData.Hex("#1A803F"),
        rgb = ColorDetailsData.Rgb("26", "128", "63"),
        hsl = ColorDetailsData.Hsl("142", "66", "30"),
        hsv = ColorDetailsData.Hsv("142", "80", "50"),
        cmyk = ColorDetailsData.Cmyk("80", "0", "51", "50"),
        exactMatch = ColorDetailsData.ExactMatch.No(
            exactValue = "#126B40",
            exactColor = ColorInt(0x126B40),
            goToExactColor = OnExactColorClick,
            deviation = "1366",
        ),
        initialColorData = null,
    )

    fun viewData0() = ColorDetailsUiData.ViewData(
        hexLabel = "HEX",
        rgbLabel = "RGB",
        hslLabel = "HSL",
        hsvLabel = "HSV",
        cmykLabel = "CMYK",
        nameLabel = "NAME",
        exactMatchLabel = "EXACT MATCH",
        exactMatchYes = "Yes",
        exactMatchNo = "No",
        exactValueLabel = "EXACT VALUE",
        deviationLabel = "DEVIATION",
        viewColorSchemeButtonText = "View color scheme",
        goBackToInitialColorButtonText = "Go back",
    )

    fun uiData0() =
        ColorDetailsUiData(
            headline = "Jewel",
            translations = ColorDetailsUiData.ColorTranslations(
                hex = ColorDetailsUiData.ColorTranslation.Hex(
                    label = "HEX",
                    value = "#1A803F",
                ),
                rgb = ColorDetailsUiData.ColorTranslation.Rgb(
                    label = "RGB",
                    r = "26",
                    g = "128",
                    b = "63",
                ),
                hsl = ColorDetailsUiData.ColorTranslation.Hsl(
                    label = "HSL",
                    h = "142",
                    s = "66",
                    l = "30",
                ),
                hsv = ColorDetailsUiData.ColorTranslation.Hsv(
                    label = "HSV",
                    h = "142",
                    s = "80",
                    v = "50",
                ),
                cmyk = ColorDetailsUiData.ColorTranslation.Cmyk(
                    label = "CMYK",
                    c = "80",
                    m = "0",
                    y = "51",
                    k = "50",
                ),
            ),
            specs = listOf(
                ColorDetailsUiData.ColorSpec.Name(
                    label = "NAME",
                    value = "Jewel",
                ),
                ColorDetailsUiData.ColorSpec.ExactMatch(
                    label = "EXACT MATCH",
                    value = "No",
                    goBackToInitialColorButton = null,
                ),
                ColorDetailsUiData.ColorSpec.ExactValue(
                    label = "EXACT VALUE",
                    value = "#126B40",
                    exactColor = Color(0xFF126B40),
                    onClick = OnExactColorClick,
                ),
                ColorDetailsUiData.ColorSpec.Deviation(
                    label = "DEVIATION",
                    value = "1366",
                ),
            ),
        )

    // endregion

    // region #1

    fun data1() = ColorDetailsData(
        colorName = "Alice Blue",
        hex = ColorDetailsData.Hex("#F0F8FF"),
        rgb = ColorDetailsData.Rgb("240", "248", "255"),
        hsl = ColorDetailsData.Hsl("208", "100", "97"),
        hsv = ColorDetailsData.Hsv("208", "6", "100"),
        cmyk = ColorDetailsData.Cmyk("6", "3", "0", "0"),
        exactMatch = ColorDetailsData.ExactMatch.Yes,
        initialColorData = null,
    )

    fun viewData1() = ColorDetailsUiData.ViewData(
        hexLabel = "literal hex label",
        rgbLabel = "literal rgb label",
        hslLabel = "literal hsl label",
        hsvLabel = "literal hsv label",
        cmykLabel = "literal cmyk label",
        nameLabel = "literal name label",
        exactMatchLabel = "literal exact match label",
        exactMatchYes = "literal yes",
        exactMatchNo = "literal no",
        exactValueLabel = "literal exact value label",
        deviationLabel = "literal deviation",
        viewColorSchemeButtonText = "literal view color scheme button text",
        goBackToInitialColorButtonText = "literal go back to initial color button text",
    )

    fun uiData1() =
        ColorDetailsUiData(
            headline = "Alice Blue",
            translations = ColorDetailsUiData.ColorTranslations(
                hex = ColorDetailsUiData.ColorTranslation.Hex(
                    label = "literal hex label",
                    value = "#F0F8FF",
                ),
                rgb = ColorDetailsUiData.ColorTranslation.Rgb(
                    label = "literal rgb label",
                    r = "240",
                    g = "248",
                    b = "255",
                ),
                hsl = ColorDetailsUiData.ColorTranslation.Hsl(
                    label = "literal hsl label",
                    h = "208",
                    s = "100",
                    l = "97",
                ),
                hsv = ColorDetailsUiData.ColorTranslation.Hsv(
                    label = "literal hsv label",
                    h = "208",
                    s = "6",
                    v = "100",
                ),
                cmyk = ColorDetailsUiData.ColorTranslation.Cmyk(
                    label = "literal cmyk label",
                    c = "6",
                    m = "3",
                    y = "0",
                    k = "0",
                ),
            ),
            specs = listOf(
                ColorDetailsUiData.ColorSpec.Name(
                    label = "literal name label",
                    value = "Alice Blue",
                ),
                ColorDetailsUiData.ColorSpec.ExactMatch(
                    label = "literal exact match label",
                    value = "literal yes",
                    goBackToInitialColorButton = null,
                ),
            ),
        )

    // endregion
}