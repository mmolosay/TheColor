package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.KeyboardArrowRight
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.LocalColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.Background
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorSpec
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslation
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ColorTranslations
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ViewColorSchemeButton
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsUiData.ViewData
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.State.Loading
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.State.Ready
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
        is Ready -> {
            val uiData = rememberUiData(state.data, viewData)
            ColorDetails(uiData)
        }
    }
}

@Composable
fun ColorDetails(
    uiData: ColorDetailsUiData,
) {
    val colors = rememberContentColors(isSurfaceDark = uiData.background.isDark)
    CompositionLocalProvider(
        LocalColorsOnTintedSurface provides colors,
        LocalContentColor provides colors.accent, // for MaterialRippleTheme
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(uiData.background.color),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
//        Spacer(modifier = Modifier.height(32.dp)) // TODO: use when wrapping fragment with its own margin is gone
            Headline(uiData.headline)

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

            Spacer(modifier = Modifier.height(16.dp))
            ViewColorSchemeButton(uiData.viewColorSchemeButtonText)
        }
    }
}

@Composable
private fun Headline(text: String) =
    Text(
        text = text,
        textAlign = TextAlign.Center,
        color = colorsOnTintedSurface.accent,
        style = MaterialTheme.typography.displayLarge,
    )

@Composable
private fun Divider() =
    MaterialDivider(
        thickness = 1.dp,
        color = colorsOnTintedSurface.muted.copy(alpha = 0.30f),
    )

@Composable
private fun ViewColorSchemeButton(uiData: ViewColorSchemeButton) {
    val colors = ButtonDefaults.outlinedButtonColors(
        contentColor = colorsOnTintedSurface.accent,
    )
    val border = ButtonDefaults.outlinedButtonBorder.copy(
        brush = SolidColor(colorsOnTintedSurface.accent),
    )
    val addedTextStyle = TextStyle(
        platformStyle = PlatformTextStyle(
            includeFontPadding = false,
        ),
        lineHeightStyle = LineHeightStyle(
            alignment = LineHeightStyle.Alignment.Proportional,
            trim = LineHeightStyle.Trim.FirstLineTop,
        )
    )
    OutlinedButton(
        onClick = uiData.onClick,
        colors = colors,
        border = border,
    ) {
        Text(
            text = uiData.text,
            style = LocalTextStyle.current.merge(addedTextStyle),
        )
        Icon(
            imageVector = Icons.Rounded.KeyboardArrowRight,
            contentDescription = null, // described by text above
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
private fun rememberUiData(
    data: ColorDetailsData,
    viewData: ViewData,
): ColorDetailsUiData =
    remember(data) { ColorDetailsUiData(data, viewData) }

@Composable
private fun rememberContentColors(isSurfaceDark: Boolean): ColorsOnTintedSurface =
    remember(isSurfaceDark) { if (isSurfaceDark) colorsOnDarkSurface() else colorsOnLightSurface() }

@Preview(showBackground = true)
@Composable
private fun PreviewLight() {
    TheColorTheme {
        ColorDetails(
            uiData = previewUiData(isBackgroundDark = true),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun PreviewDark() {
    TheColorTheme {
        ColorDetails(
            uiData = previewUiData(isBackgroundDark = false),
        )
    }
}

private fun previewUiData(
    isBackgroundDark: Boolean,
) =
    ColorDetailsUiData(
        background = Background(
            color = Color(0xFF1A803F),
            isDark = isBackgroundDark,
        ),
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
        viewColorSchemeButtonText = ViewColorSchemeButton(
            text = "View color scheme",
            onClick = {},
        ),
    )