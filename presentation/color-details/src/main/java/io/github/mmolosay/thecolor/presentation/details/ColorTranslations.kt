package io.github.mmolosay.thecolor.presentation.details

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
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslation
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslations

@Composable
internal fun ColorTranslations(uiData: ColorTranslations) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Hex(uiData.hex)
        Rgb(uiData.rgb)
        Hsl(uiData.hsl)
        Hsv(uiData.hsv)
        Cmyk(uiData.cmyk)
    }

@Composable
private fun Hex(uiData: ColorTranslation.Hex) =
    ColorTranslation(
        label = uiData.label,
        values = with(uiData) { listOf(value) },
    )

@Composable
private fun Rgb(uiData: ColorTranslation.Rgb) =
    ColorTranslation(
        label = uiData.label,
        values = with(uiData) { listOf(r, g, b) },
    )

@Composable
private fun Hsl(uiData: ColorTranslation.Hsl) =
    ColorTranslation(
        label = uiData.label,
        values = with(uiData) { listOf(h, s, l) },
    )

@Composable
private fun Hsv(uiData: ColorTranslation.Hsv) =
    ColorTranslation(
        label = uiData.label,
        values = with(uiData) { listOf(h, s, v) },
    )

@Composable
private fun Cmyk(uiData: ColorTranslation.Cmyk) =
    ColorTranslation(
        label = uiData.label,
        values = with(uiData) { listOf(c, m, y, k) },
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
        ColorTranslations(uiData = previewUiData())
    }
}

private fun previewUiData() =
    ColorTranslations(
        hex = ColorTranslation.Hex(
            label = "HEX",
            value = "#1A803F",
        ),
        rgb = ColorTranslation.Rgb(
            label = "RGB",
            r = "26",
            g = "128",
            b = "63",
        ),
        hsl = ColorTranslation.Hsl(
            label = "HSL",
            h = "142",
            s = "66",
            l = "30",
        ),
        hsv = ColorTranslation.Hsv(
            label = "HSV",
            h = "142",
            s = "80",
            v = "50",
        ),
        cmyk = ColorTranslation.Cmyk(
            label = "CMYK",
            c = "80",
            m = "0",
            y = "51",
            k = "50",
        ),
    )