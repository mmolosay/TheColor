package io.github.mmolosay.thecolor.presentation.details.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData

@Composable
internal fun ColorTranslations(
    hex: ColorDetailsData.Hex,
    rgb: ColorDetailsData.Rgb,
    hsl: ColorDetailsData.Hsl,
    hsv: ColorDetailsData.Hsv,
    cmyk: ColorDetailsData.Cmyk,
    strings: ColorDetailsUiStrings,
) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Hex(
            label = strings.hexLabel,
            value = hex.value,
        )
        Rgb(
            label = strings.rgbLabel,
            rValue = rgb.r,
            gValue = rgb.g,
            bValue = rgb.b,
        )
        Hsl(
            label = strings.hslLabel,
            hValue = hsl.h,
            sValue = hsl.s,
            lValue = hsl.l,
        )
        Hsv(
            label = strings.hsvLabel,
            hValue = hsv.h,
            sValue = hsv.s,
            vValue = hsv.v,
        )
        Cmyk(
            label = strings.cmykLabel,
            cValue = cmyk.c,
            mValue = cmyk.m,
            yValue = cmyk.y,
            kValue = cmyk.k,
        )
    }

@Composable
private fun Hex(
    label: String,
    value: String,
) =
    ColorTranslation(
        label = label,
        values = listOf(value),
    )

@Composable
private fun Rgb(
    label: String,
    rValue: String,
    gValue: String,
    bValue: String,
) =
    ColorTranslation(
        label = label,
        values = listOf(rValue, gValue, bValue),
    )

@Composable
private fun Hsl(
    label: String,
    hValue: String,
    sValue: String,
    lValue: String,
) =
    ColorTranslation(
        label = label,
        values = listOf(hValue, sValue, lValue),
    )

@Composable
private fun Hsv(
    label: String,
    hValue: String,
    sValue: String,
    vValue: String,
) =
    ColorTranslation(
        label = label,
        values = listOf(hValue, sValue, vValue),
    )

@Composable
private fun Cmyk(
    label: String,
    cValue: String,
    mValue: String,
    yValue: String,
    kValue: String,
) =
    ColorTranslation(
        label = label,
        values = listOf(cValue, mValue, yValue, kValue),
    )

@Composable
private fun ColorTranslation(
    label: String,
    values: List<String>,
) =
    Row {
        Label(text = label)
        Spacer(modifier = Modifier.width(12.dp))
        RowOfValues {
            values.forEach { value ->
                Value(text = value)
            }
        }
    }

@Composable
private fun Label(
    text: String,
) =
    Text(
        text = text,
        color = colorsOnTintedSurface.muted,
    )

@Composable
private fun Value(
    text: String,
) =
    Text(
        text = text,
        color = colorsOnTintedSurface.accent,
    )

@Composable
private fun RowOfValues(
    content: @Composable RowScope.() -> Unit,
) =
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        content = content,
    )

@Preview(showBackground = true)
@Composable
private fun PreviewLight() {
    TheColorTheme {
        ProvideColorsOnTintedSurface(colorsOnLightSurface()) {
            ColorTranslations(
                hex = ColorDetailsData.Hex(
                    value = "#1A803F",
                ),
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
                strings = ColorDetailsUiStrings(
                    hexLabel = "HEX",
                    rgbLabel = "RGB",
                    hslLabel = "HSL",
                    hsvLabel = "HSV",
                    cmykLabel = "CMYK",
                    nameLabel = "_",
                    exactMatchLabel = "_",
                    exactMatchYes = "_",
                    exactMatchNo = "_",
                    goBackToSeedColorButtonText = "_",
                    exactValueLabel = "_",
                    deviationLabel = "_",
                ),
            )
        }
    }
}