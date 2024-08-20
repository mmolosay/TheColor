package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.ui.graphics.Color
import io.github.mmolosay.thecolor.domain.model.ColorScheme.Mode
import io.github.mmolosay.thecolor.presentation.impl.ColorInt
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockkObject
import io.mockk.unmockkObject
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.junit.runners.Parameterized

@RunWith(Parameterized::class)
class ColorSchemeUiDataFactoryTest(
    val data: ColorSchemeData,
    val viewData: ColorSchemeUiData.ViewData,
    val expectedUiData: ColorSchemeUiData,
) {

    @Before
    fun before() {
        mockkObject(ColorSchemeUiDataComponents)
        every {
            ColorSchemeUiDataComponents.OnSwatchClick(
                data = any(),
                swatchIndex = any(),
            )
        } returns TestParameters.onSwatchClick
        every {
            ColorSchemeUiDataComponents.OnModeSelect(
                data = any(),
                mode = any(),
            )
        } returns TestParameters.onModeSelect
        every {
            ColorSchemeUiDataComponents.OnSwatchCountSelect(
                data = any(),
                count = any(),
            )
        } returns TestParameters.onSwatchCountSelect
    }

    @After
    fun after() {
        unmockkObject(ColorSchemeUiDataComponents)
    }

    @Test
    fun `combining ViewModel data and View data produces expected UI data`() {
        val uiData = ColorSchemeUiData(data, viewData)

        uiData shouldBe expectedUiData
    }


    companion object {

        @JvmStatic
        @Parameterized.Parameters
        fun data() = with(TestParameters) {
            listOf(
                /* #0  */ arrayOf(data0(), viewData0(), uiData0()),
            )
        }
    }
}

private object TestParameters {

    // inline lambdas like '{}' aren't equal, so reusing the same instance
    val onSwatchClick: () -> Unit = {}
    val onModeSelect: () -> Unit = {}
    val onSwatchCountSelect: () -> Unit = {}
    val applyChanges: () -> Unit = {}

    // region #0

    fun data0() =
        ColorSchemeData(
            swatches = listOf(
                ColorSchemeData.Swatch(
                    color = ColorInt(0x05160B),
                    isDark = true,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x0A2D17),
                    isDark = true,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x0F4522),
                    isDark = true,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x135C2E),
                    isDark = true,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x187439),
                    isDark = true,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x1C8C45),
                    isDark = false,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x20A450),
                    isDark = false,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x24BC5C),
                    isDark = false,
                ),
                ColorSchemeData.Swatch(
                    color = ColorInt(0x28D567),
                    isDark = false,
                ),
            ),
            onSwatchSelect = {},
            activeMode = Mode.Monochrome,
            selectedMode = Mode.Analogic,
            onModeSelect = {},
            activeSwatchCount = ColorSchemeData.SwatchCount.Six,
            selectedSwatchCount = ColorSchemeData.SwatchCount.Nine,
            onSwatchCountSelect = {},
            changes = ColorSchemeData.Changes.Present(applyChanges),
        )

    fun viewData0() =
        ColorSchemeUiData.ViewData(
            modeLabel = "Mode",
            modeMonochromeName = "Monochrome",
            modeMonochromeDarkName = "Monochrome Dark",
            modeMonochromeLightName = "Monochrome Light",
            modeAnalogicName = "Analogic",
            modeComplementName = "Complement",
            modeAnalogicComplementName = "Analogic-Complement",
            modeTriadName = "Triad",
            modeQuadName = "Quad",
            swatchCountLabel = "Swatch count",
            applyChangesButtonText = "Apply changes",
        )

    fun uiData0() =
        ColorSchemeUiData(
            swatches = listOf(
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_05160B),
                    useLightContentColors = true,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_0A2D17),
                    useLightContentColors = true,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_0F4522),
                    useLightContentColors = true,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_135C2E),
                    useLightContentColors = true,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_187439),
                    useLightContentColors = true,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_1C8C45),
                    useLightContentColors = false,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_20A450),
                    useLightContentColors = false,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_24BC5C),
                    useLightContentColors = false,
                    onClick = onSwatchClick,
                ),
                ColorSchemeUiData.Swatch(
                    color = Color(0xFF_28D567),
                    useLightContentColors = false,
                    onClick = onSwatchClick,
                ),
            ),
            modeSection = ColorSchemeUiData.ModeSection(
                label = "Mode",
                value = "Monochrome",
                modes = listOf(
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Monochrome",
                        isSelected = false,
                        onSelect = onModeSelect,
                    ),
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Monochrome Dark",
                        isSelected = false,
                        onSelect = onModeSelect,
                    ),
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Monochrome Light",
                        isSelected = false,
                        onSelect = onModeSelect,
                    ),
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Analogic",
                        isSelected = true,
                        onSelect = onModeSelect,
                    ),
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Complement",
                        isSelected = false,
                        onSelect = onModeSelect,
                    ),
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Analogic-Complement",
                        isSelected = false,
                        onSelect = onModeSelect,
                    ),
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Triad",
                        isSelected = false,
                        onSelect = onModeSelect,
                    ),
                    ColorSchemeUiData.ModeSection.Mode(
                        name = "Quad",
                        isSelected = false,
                        onSelect = onModeSelect,
                    ),
                ),
            ),
            swatchCountSection = ColorSchemeUiData.SwatchCountSection(
                label = "Swatch count",
                value = "6",
                swatchCountItems = listOf(
                    ColorSchemeUiData.SwatchCountSection.SwatchCount(
                        text = "3",
                        isSelected = false,
                        onSelect = onSwatchCountSelect,
                    ),
                    ColorSchemeUiData.SwatchCountSection.SwatchCount(
                        text = "4",
                        isSelected = false,
                        onSelect = onSwatchCountSelect,
                    ),
                    ColorSchemeUiData.SwatchCountSection.SwatchCount(
                        text = "6",
                        isSelected = false,
                        onSelect = onSwatchCountSelect,
                    ),
                    ColorSchemeUiData.SwatchCountSection.SwatchCount(
                        text = "9",
                        isSelected = true,
                        onSelect = onSwatchCountSelect,
                    ),
                    ColorSchemeUiData.SwatchCountSection.SwatchCount(
                        text = "13",
                        isSelected = false,
                        onSelect = onSwatchCountSelect,
                    ),
                    ColorSchemeUiData.SwatchCountSection.SwatchCount(
                        text = "18",
                        isSelected = false,
                        onSelect = onSwatchCountSelect,
                    ),
                ),
            ),
            applyChangesButton = ColorSchemeUiData.ApplyChangesButton.Visible(
                text = "Apply changes",
                onClick = applyChanges,
            ),
        )

    // endregion
}