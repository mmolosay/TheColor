package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel.DataState
import io.github.mmolosay.thecolor.presentation.errors.ErrorMessageWithButton
import io.github.mmolosay.thecolor.presentation.errors.messageOrUnknown
import io.github.mmolosay.thecolor.presentation.errors.rememberDefaultErrorsUiStrings
import io.github.mmolosay.thecolor.utils.doNothing

@Composable
fun ColorDetails(
    viewModel: ColorDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val dataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    ColorDetails(
        dataState = dataState,
        modifier = modifier,
    )
}

@Composable
fun ColorDetails(
    dataState: DataState,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorDetailsUiStrings(context) }
    when (dataState) {
        is DataState.Idle -> {
            doNothing() // Color Details shouldn't be visible at Home at this point
        }
        is DataState.Loading -> {
            ColorDetailsLoading()
        }
        is DataState.Ready -> {
            ColorDetails(
                data = dataState.data,
                strings = strings,
                modifier = modifier,
            )
        }
        is DataState.Error -> {
            Error(error = dataState.error)
        }
    }
}

@Composable
fun ColorDetails(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Headline(
            text = data.colorName,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
        ColorTranslations(
            hex = data.hex,
            rgb = data.rgb,
            hsl = data.hsl,
            hsv = data.hsv,
            cmyk = data.cmyk,
            strings = strings,
        )

        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
        ) {
            Divider()

            Spacer(modifier = Modifier.height(16.dp))
            ColorSpecs(
                modifier = Modifier.align(Alignment.Start),
                colorName = data.colorName,
                exactMatch = data.exactMatch,
                colorRoleData = data.colorRoleData,
                strings = strings,
            )
        }
    }
}

@Composable
private fun Headline(
    text: String,
    modifier: Modifier = Modifier,
) =
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = colorsOnTintedSurface.accent,
        style = MaterialTheme.typography.displayLarge,
    )

@Composable
private fun Divider() =
    HorizontalDivider(
        thickness = 1.dp,
        color = colorsOnTintedSurface.muted.copy(alpha = 0.30f)
    )

@Composable
private fun Error(
    error: ColorDetailsError,
) {
    val strings = rememberDefaultErrorsUiStrings()
    ErrorMessageWithButton(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        message = error.cause.messageOrUnknown(strings),
        button = {
            val colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorsOnTintedSurface.accent,
            )
            val border = ButtonDefaults.outlinedButtonBorder().copy(
                brush = SolidColor(colorsOnTintedSurface.accent),
            )
            OutlinedButton(
                onClick = error.tryAgain,
                colors = colors,
                border = border,
            ) {
                Text(text = strings.actionTryAgain)
            }
        },
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewLight() {
    TheColorTheme {
        val colors = remember { colorsOnDarkSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorDetailsWithPreviewData(
                modifier = Modifier.background(Color(0xFF_1A803F)),
                data = previewDataSeed(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDark() {
    TheColorTheme {
        val colors = remember { colorsOnLightSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorDetailsWithPreviewData(
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
                data = previewDataSeed(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExactLight() {
    TheColorTheme {
        val colors = remember { colorsOnDarkSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorDetailsWithPreviewData(
                modifier = Modifier.background(Color(0xFF_126B40)),
                data = previewDataExact(),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewExactDark() {
    TheColorTheme {
        val colors = remember { colorsOnLightSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorDetailsWithPreviewData(
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
                data = previewDataExact(),
            )
        }
    }
}

@Composable
private fun ColorDetailsWithPreviewData(
    modifier: Modifier,
    data: ColorDetailsData,
) {
    ColorDetails(
        modifier = modifier,
        data = data,
        strings = previewUiStrings(),
    )
}

private fun previewDataSeed() =
    ColorDetailsData(
        colorName = "Jewel",
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
        exactMatch = ColorDetailsData.ExactMatch.No(
            exactValue = "#126B40",
            exactColor = ColorInt(0x126B40),
            deviation = "1366",
        ),
        colorRoleData = ColorDetailsData.ColorRoleData.Seed(
            exactColor = ColorInt(0x126B40),
            selectExactColor = {},
        ),
    )

private fun previewDataExact() =
    ColorDetailsData(
        colorName = "Jewel",
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
        exactMatch = ColorDetailsData.ExactMatch.Yes,
        colorRoleData = ColorDetailsData.ColorRoleData.Exact(
            seedColor = ColorInt(0x1A803F),
            selectSeedColor = {},
        ),
    )

private fun previewUiStrings() =
    ColorDetailsUiStrings(
        hexLabel = "HEX",
        rgbLabel = "RGB",
        hslLabel = "HSL",
        hsvLabel = "HSV",
        cmykLabel = "CMYK",
        nameLabel = "NAME",
        exactMatchLabel = "EXACT MATCH",
        exactMatchYes = "Yes",
        exactMatchNo = "No",
        goBackToSeedColorButtonText = "Go back to",
        exactValueLabel = "EXACT VALUE",
        deviationLabel = "DEVIATION",
    )