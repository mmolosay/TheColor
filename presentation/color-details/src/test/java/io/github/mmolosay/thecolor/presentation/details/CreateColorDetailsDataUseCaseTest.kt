package io.github.mmolosay.thecolor.presentation.details

import io.github.mmolosay.thecolor.domain.model.Color
import io.github.mmolosay.thecolor.domain.model.ColorDetails
import io.github.mmolosay.thecolor.presentation.ColorInt
import io.github.mmolosay.thecolor.presentation.ColorToColorIntUseCase
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.junit.Test

class CreateColorDetailsDataUseCaseTest {

    val colorToColorInt: ColorToColorIntUseCase = mockk()

    lateinit var sut: CreateColorDetailsDataUseCase

    @Test
    fun `creates correct data`() {
        every { with(colorToColorInt) { any<Color>().toColorInt() } } returns ColorInt(0x123456)
        createSut()

        val details = ColorDetails()
        val resultData = sut.invoke(
            details = details,
            goToExactColor = {},
            initialColor = null,
            goToInitialColor = {},
        )

        val comparableData = resultData.copyWithNoopLambdas()
        comparableData shouldBe ColorDetailsData(
            colorName = "Jewel",
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
                exactColor = ColorInt(0x123456),
                goToExactColor = NoopOnClickAction,
                deviation = "1366",
            ),
            initialColorData = null,
        )
    }

    fun createSut() =
        CreateColorDetailsDataUseCase(
            colorToColorInt = colorToColorInt,
        ).also {
            sut = it
        }

    // Arrow lenses cannot be used properly: copy {} doesn't seem to work with my Kotlin version
    // https://arrow-kt.io/learn/immutable-data/lens/#more-powerful-copy
    fun ColorDetailsData.copyWithNoopLambdas() =
        this.copy(
            exactMatch = exactMatch.run {
                when (this) {
                    is ColorDetailsData.ExactMatch.No -> this.copy(
                        goToExactColor = NoopOnClickAction,
                    )
                    is ColorDetailsData.ExactMatch.Yes -> this
                }
            },
        )

    object NoopOnClickAction : () -> Unit {
        override fun invoke() {}
    }
}

private fun ColorDetails() =
    ColorDetails(
        color = Color.Hex(0x1A803F),
        colorHexString = ColorDetails.ColorHexString(
            withNumberSign = "#1A803F",
            withoutNumberSign = "1A803F",
        ),
        colorTranslations = ColorDetails.ColorTranslations(
            rgb = ColorDetails.ColorTranslation.Rgb(
                standard = ColorDetails.ColorTranslation.Rgb.StandardValues(
                    r = 26,
                    g = 128,
                    b = 63,
                ),
                normalized = ColorDetails.ColorTranslation.Rgb.NormalizedValues(
                    r = 0.101960786f,
                    g = 0.5019608f,
                    b = 0.24705882f,
                ),
            ),
            hsl = ColorDetails.ColorTranslation.Hsl(
                standard = ColorDetails.ColorTranslation.Hsl.StandardValues(
                    h = 142,
                    s = 66,
                    l = 30,
                ),
                normalized = ColorDetails.ColorTranslation.Hsl.NormalizedValues(
                    h = 0.39379084f,
                    s = 0.66233766f,
                    l = 0.3019608f,
                ),
            ),
            hsv = ColorDetails.ColorTranslation.Hsv(
                standard = ColorDetails.ColorTranslation.Hsv.StandardValues(
                    h = 142,
                    s = 80,
                    v = 50,
                ),
                normalized = ColorDetails.ColorTranslation.Hsv.NormalizedValues(
                    h = 0.39379084f,
                    s = 0.796875f,
                    v = 0.5019608f,
                ),
            ),
            xyz = ColorDetails.ColorTranslation.Xyz(
                standard = ColorDetails.ColorTranslation.Xyz.StandardValues(
                    x = 27,
                    y = 40,
                    z = 30,
                ),
                normalized = ColorDetails.ColorTranslation.Xyz.NormalizedValues(
                    x = 0.26614392f,
                    y = 0.39851686f,
                    z = 0.29663098f,
                ),
            ),
            cmyk = ColorDetails.ColorTranslation.Cmyk(
                standard = ColorDetails.ColorTranslation.Cmyk.StandardValues(
                    c = 80,
                    m = 0,
                    y = 51,
                    k = 50,
                ),
                normalized = ColorDetails.ColorTranslation.Cmyk.NormalizedValues(
                    c = 0.796875f,
                    m = 0.0f,
                    y = 0.5078125f,
                    k = 0.49803922f,
                ),
            ),
        ),
        colorName = "Jewel",
        exact = ColorDetails.Exact(
            color = Color.Hex(0x126B40),
            hexStringWithNumberSign = "#126B40",
        ),
        matchesExact = false,
        distanceFromExact = 1366,
    )