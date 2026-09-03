package io.github.mmolosay.thecolor.presentation.details.ui

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
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsAction
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsError
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsFacade
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsHandle
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorRole
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ExecuteColorDetailsAction
import io.github.mmolosay.thecolor.presentation.errors.ErrorMessageWithButton
import io.github.mmolosay.thecolor.presentation.errors.messageOrUnknown
import io.github.mmolosay.thecolor.presentation.errors.rememberDefaultErrorsUiStrings
import io.github.mmolosay.thecolor.utils.doNothing
import kotlinx.coroutines.Job

@Suppress("unused") // example of having 'ViewModel' as entry point
@Composable
fun ColorDetails(
    viewModel: ColorDetailsViewModel,
    modifier: Modifier = Modifier,
) {
    val facade = rememberColorDetailsFacade(viewModel)
    ColorDetails(
        facade = facade,
        modifier = modifier,
    )
}

@Composable
fun rememberColorDetailsFacade(viewModel: ColorDetailsViewModel): ColorDetailsFacade {
    val handle = remember(viewModel) { ColorDetailsHandle(viewModel) }
    val state = viewModel.stateFlow.collectAsStateWithLifecycle().value
    return remember(handle, state) { handle.facade(state) }
}

@Composable
fun ColorDetails(
    facade: ColorDetailsFacade,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorDetailsUiStrings(context) }
    val execute = facade.execute
    when (val state = facade.state) {
        is ColorDetailsState.Idle -> {
            doNothing() // Color Details shouldn't be visible at Home at this point
        }
        is ColorDetailsState.Loading -> {
            ColorDetailsLoading()
        }
        is ColorDetailsState.Ready -> {
            ColorDetails(
                data = state.data,
                strings = strings,
                execute = execute,
                modifier = modifier,
            )
        }
        is ColorDetailsState.Error -> {
            Error(
                error = state.error,
                onTryAgainClick = { execute(ColorDetailsAction.RetryOnError) },
            )
        }
    }
}

@Composable
fun ColorDetails(
    data: ColorDetailsData,
    strings: ColorDetailsUiStrings,
    execute: ExecuteColorDetailsAction,
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
                onSelectSeedColorClick = {
                    val action = ColorDetailsAction.SelectColor(role = ColorRole.Seed)
                    execute(action)
                },
                onSelectExactColorClick = {
                    val action = ColorDetailsAction.SelectColor(role = ColorRole.Exact)
                    execute(action)
                },
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
    onTryAgainClick: () -> Unit,
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
                onClick = onTryAgainClick,
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
        execute = { Job() },
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