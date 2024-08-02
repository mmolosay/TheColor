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
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorSpec
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslation
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslations
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ViewData
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.DataState
import io.github.mmolosay.thecolor.presentation.errors.ErrorMessageWithButton
import io.github.mmolosay.thecolor.presentation.errors.message
import io.github.mmolosay.thecolor.presentation.errors.rememberDefaultErrorViewData

@Composable
fun ColorDetails(
    viewModel: ColorDetailsViewModel,
) {
    val state = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    ColorDetails(
        state = state,
    )
}

@Composable
fun ColorDetails(
    state: DataState,
) {
    val viewData = rememberViewData()
    when (state) {
        is DataState.Idle ->
            Unit // Color Details shouldn't be visible at Home at this point
        is DataState.Loading ->
            ColorDetailsLoading()
        is DataState.Ready -> {
            val uiData = rememberUiData(state.data, viewData)
            ColorDetails(uiData)
        }
        is DataState.Error ->
            Error(error = state.error)
    }
}

@Composable
fun ColorDetails(
    uiData: ColorDetailsUiData,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Headline(
            text = uiData.headline,
            modifier = Modifier.padding(horizontal = 16.dp),
        )

        Spacer(modifier = Modifier.height(24.dp))
        ColorTranslations(uiData.translations)

        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
        ) {
            Divider()

            Spacer(modifier = Modifier.height(16.dp))
            ColorSpecs(
                specs = uiData.specs,
                modifier = Modifier.align(Alignment.Start),
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
    val viewData = rememberDefaultErrorViewData()
    ErrorMessageWithButton(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth(),
        message = error.type.message(viewData),
        button = {
            val colors = ButtonDefaults.outlinedButtonColors(
                contentColor = colorsOnTintedSurface.accent,
            )
            val border = ButtonDefaults.outlinedButtonBorder.copy(
                brush = SolidColor(colorsOnTintedSurface.accent),
            )
            OutlinedButton(
                onClick = error.tryAgain,
                colors = colors,
                border = border,
            ) {
                Text(text = viewData.actionTryAgain)
            }
        },
    )
}

@Composable
private fun rememberViewData(): ViewData {
    val context = LocalContext.current
    return remember { ColorDetailsViewData(context) }
}

@Composable
private fun rememberUiData(
    data: ColorDetailsData,
    viewData: ViewData,
): ColorDetailsUiData =
    remember(data) { ColorDetailsUiData(data, viewData) }

@Preview(showBackground = true)
@Composable
private fun PreviewLight() {
    TheColorTheme {
        val colors = remember { colorsOnDarkSurface() }
        ProvideColorsOnTintedSurface(colors) {
            ColorDetails(
                uiData = previewUiData(),
                modifier = Modifier.background(Color(0xFF_1A803F)),
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
            ColorDetails(
                uiData = previewUiData(),
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
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
            ColorDetails(
                uiData = previewExactUiData(),
                modifier = Modifier.background(Color(0xFF_126B40)),
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
            ColorDetails(
                uiData = previewExactUiData(),
                modifier = Modifier.background(Color(0xFF_F0F8FF)),
            )
        }
    }
}

private fun previewUiData() =
    ColorDetailsUiData(
        headline = "Jewel",
        translations = ColorTranslations(
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
        ),
        specs = listOf(
            ColorSpec.Name(
                label = "NAME",
                value = "Jewel",
            ),
            ColorSpec.ExactMatch(
                label = "EXACT MATCH",
                value = "No",
                goBackToInitialColorButton = null,
            ),
            ColorSpec.ExactValue(
                label = "EXACT VALUE",
                value = "#126B40",
                exactColor = Color(0xFF126B40),
                onClick = {},
            ),
            ColorSpec.Deviation(
                label = "DEVIATION",
                value = "1366",
            ),
        ),
    )

private fun previewExactUiData() =
    ColorDetailsUiData(
        headline = "Jewel",
        translations = ColorTranslations(
            hex = ColorTranslation.Hex(
                label = "HEX",
                value = "#126B40",
            ),
            rgb = ColorTranslation.Rgb(
                label = "RGB",
                r = "18",
                g = "107",
                b = "64",
            ),
            hsl = ColorTranslation.Hsl(
                label = "HSL",
                h = "151",
                s = "71",
                l = "25",
            ),
            hsv = ColorTranslation.Hsv(
                label = "HSV",
                h = "151",
                s = "83",
                v = "42",
            ),
            cmyk = ColorTranslation.Cmyk(
                label = "CMYK",
                c = "83",
                m = "0",
                y = "40",
                k = "58",
            ),
        ),
        specs = listOf(
            ColorSpec.Name(
                label = "NAME",
                value = "Jewel",
            ),
            ColorSpec.ExactMatch(
                label = "EXACT MATCH",
                value = "Yes",
                goBackToInitialColorButton = ColorSpec.ExactMatch.GoBackToInitialColorButton(
                    text = "Go back to",
                    initialColor = Color(0xFF1A803f),
                    onClick = {},
                )
            ),
        ),
    )