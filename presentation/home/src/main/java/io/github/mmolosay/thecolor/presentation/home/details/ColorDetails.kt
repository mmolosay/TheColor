package io.github.mmolosay.thecolor.presentation.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ContentColors
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.Divider
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.Headline
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ViewColorSchemeButton
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ViewData
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsViewModel.State.Loading
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsViewModel.State.Ready
import androidx.compose.material3.Divider as MaterialDivider

@Composable
fun ColorDetails(
    vm: ColorDetailsViewModel,
) {
    val state = vm.dataState.collectAsStateWithLifecycle().value
    val viewData = rememberViewData()
    when (state) {
        is Loading ->
            Loading()
        is Ready ->
            ColorDetails(state.data, viewData)
    }
}

@Composable
fun ColorDetails(
    data: ColorDetailsData,
    viewData: ViewData,
) {
    val colors = rememberContentColors(useLight = data.useLightContentColors)
    val uiData = rememberUiData(data, viewData, colors)
    ColorDetails(uiData)
}

@Composable
fun ColorDetails(
    uiData: ColorDetailsUiData,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(uiData.background),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(32.dp))
        Headline(uiData.headline)

        Spacer(modifier = Modifier.height(24.dp))
        ColorTranslations(uiData.translations)

        Spacer(modifier = Modifier.height(24.dp))
        Column(
            modifier = Modifier.fillMaxWidth(0.75f),
        ) {
            Divider(uiData.divider)

            Spacer(modifier = Modifier.height(16.dp))
            ColorSpecs(
                specs = uiData.specs,
                modifier = Modifier.align(Alignment.Start),
            )
        }

        Spacer(modifier = Modifier.height(16.dp))
        ViewColorSchemeButton(uiData.viewColorSchemeButtonText)
    }
}

@Composable
private fun Headline(uiData: Headline) =
    Text(
        text = uiData.text,
        textAlign = TextAlign.Center,
        color = uiData.color,
        style = MaterialTheme.typography.displayLarge,
    )

@Composable
private fun Divider(uiData: Divider) =
    MaterialDivider(
        thickness = 1.dp,
        color = uiData.color,
    )

@Composable
private fun ViewColorSchemeButton(uiData: ViewColorSchemeButton) {
    val colors = ButtonDefaults.outlinedButtonColors(
        contentColor = uiData.contentColor,
    )
    val border = ButtonDefaults.outlinedButtonBorder.copy(
        brush = SolidColor(uiData.contentColor),
    )
    OutlinedButton(
        onClick = uiData.onClick,
        colors = colors,
        border = border,
    ) {
        Text(
            text = uiData.text,
        )
    }
}

@Composable
private fun Loading() =
    CircularProgressIndicator(
        modifier = Modifier
            .fillMaxSize()
            .wrapContentSize(),
    )

@Composable
private fun rememberViewData(): ViewData {
    val context = LocalContext.current
    return remember { ColorDetailsViewData(context) }
}

@Composable
private fun rememberContentColors(useLight: Boolean): ContentColors =
    remember(useLight) { ColorDetailsContentColors(useLight) }

@Composable
private fun rememberUiData(
    data: ColorDetailsData,
    viewData: ViewData,
    colors: ContentColors,
): ColorDetailsUiData =
    remember(data, colors) { ColorDetailsUiData(data, viewData, colors) }

@Preview(showBackground = true)
@Composable
private fun PreviewLight() {
    TheColorTheme {
        ColorDetails(
            data = previewData(useLightContentColors = true),
            viewData = previewViewData(),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDark() {
    TheColorTheme {
        ColorDetails(
            data = previewData(useLightContentColors = false),
            viewData = previewViewData(),
        )
    }
}

private fun previewData(
    useLightContentColors: Boolean,
) =
    ColorDetailsData(
        color = ColorDetailsData.ColorInt(0x1A803F),
        colorName = "Jewel",
        useLightContentColors = useLightContentColors,
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
            exactColor = ColorDetailsData.ColorInt(0x126B40),
            onExactClick = {},
            deviation = "1366"
        ),
        onViewColorSchemeClick = {},
    )

private fun previewViewData() =
    ViewData(
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
    )