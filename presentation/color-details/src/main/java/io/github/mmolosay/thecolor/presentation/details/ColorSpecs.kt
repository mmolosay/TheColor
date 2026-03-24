package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.colorint.toCompose
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
internal fun ColorSpecs(
    colorName: String,
    exactMatch: ColorDetailsData.ExactMatch,
    colorRoleData: ColorDetailsData.ColorRoleData,
    strings: ColorDetailsUiStrings,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Name(
            label = strings.nameLabel,
            name = colorName,
        )

        ExactMatch(
            exactMatch = exactMatch,
            colorRoleData = colorRoleData,
            strings = strings,
        )

        val isNotExactMatch = (exactMatch is ColorDetailsData.ExactMatch.No)
        val isSeedColor = (colorRoleData is ColorDetailsData.ColorRoleData.Seed)
        if (isNotExactMatch && isSeedColor) {
            ExactValue(
                exactMatch = exactMatch,
                colorRoleData = colorRoleData,
                strings = strings,
            )
            Deviation(
                label = strings.deviationLabel,
                value = exactMatch.deviation,
            )
        }
    }
}

@Composable
private fun Name(
    label: String,
    name: String,
) {
    Column {
        Label(text = label)
        Value(text = name)
    }
}

@Composable
private fun ExactMatch(
    exactMatch: ColorDetailsData.ExactMatch,
    colorRoleData: ColorDetailsData.ColorRoleData,
    strings: ColorDetailsUiStrings,
) {
    val value = when (exactMatch) {
        is ColorDetailsData.ExactMatch.Yes -> strings.exactMatchYes
        is ColorDetailsData.ExactMatch.No -> strings.exactMatchNo
    }
    val goToSeedColorButton: (@Composable () -> Unit)? =
        if (colorRoleData is ColorDetailsData.ColorRoleData.Exact) {
            {
                GoToSeedColorButton(
                    onClick = colorRoleData.goToSeedColor,
                    text = strings.goBackToSeedColorButtonText,
                    seedColor = colorRoleData.seedColor.toCompose(),
                )
            }
        } else null
    ExactMatch(
        label = strings.exactMatchLabel,
        value = value,
        goToSeedColorButton = goToSeedColorButton,
    )
}

@Composable
private fun ExactMatch(
    label: String,
    value: String,
    goToSeedColorButton: (@Composable () -> Unit)?,
) {
    Row {
        Column {
            Label(text = label)
            Value(text = value)
        }
        if (goToSeedColorButton != null) {
            Spacer(modifier = Modifier.weight(1f))
            goToSeedColorButton()
        }
    }
}

@Composable
private fun GoToSeedColorButton(
    onClick: () -> Unit,
    text: String,
    seedColor: Color,
) {
    val colors = ButtonDefaults.outlinedButtonColors(
        contentColor = colorsOnTintedSurface.accent,
    )
    val border = ButtonDefaults.outlinedButtonBorder().copy(
        brush = SolidColor(colorsOnTintedSurface.muted),
    )
    OutlinedButton(
        onClick = onClick,
        colors = colors,
        border = border,
    ) {
        Text(
            text = text,
        )
        Spacer(modifier = Modifier.width(4.dp))
        ColorPreview(
            modifier = Modifier.padding(top = 1.dp),
            color = seedColor,
        )
    }
}

@Composable
private fun ExactValue(
    exactMatch: ColorDetailsData.ExactMatch.No,
    colorRoleData: ColorDetailsData.ColorRoleData.Seed,
    strings: ColorDetailsUiStrings,
) {
    ExactValue(
        label = strings.exactValueLabel,
        exactColorValue = exactMatch.exactValue,
        goToExactColor = colorRoleData.goToExactColor,
        exactColor = colorRoleData.exactColor.toCompose(),
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExactValue(
    label: String,
    exactColorValue: String,
    goToExactColor: () -> Unit,
    exactColor: Color,
) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Label(text = label)
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentSize provides Dp.Unspecified,
            ) {
                val colors = IconButtonDefaults.iconButtonColors(
                    contentColor = colorsOnTintedSurface.accent,
                )
                IconButton(
                    onClick = goToExactColor,
                    modifier = Modifier.size(20.dp),
                    colors = colors,
                ) {
                    Icon(
                        modifier = Modifier.padding(all = 4.dp),
                        painter = painterResource(DesignR.drawable.ic_open_in_new),
                        contentDescription = stringResource(R.string.color_details_exact_value_icon_content_desc),
                    )
                }
            }
        }
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            Value(text = exactColorValue)
            ColorPreview(color = exactColor)
        }
    }
}

@Composable
private fun ColorPreview(
    color: Color,
    modifier: Modifier = Modifier,
) =
    Box(
        modifier = modifier
            .size(13.dp)
            .clip(CircleShape)
            .background(color)
    )

@Composable
private fun Deviation(
    label: String,
    value: String,
) {
    Column {
        Label(text = label)
        Value(text = value)
    }
}

@Composable
private fun Label(
    text: String,
) =
    Text(
        text = text,
        color = colorsOnTintedSurface.muted,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 0.1666.em,
        )
    )

@Composable
private fun Value(
    text: String,
) =
    Text(
        text = text,
        color = colorsOnTintedSurface.accent,
        style = MaterialTheme.typography.bodyLarge,
    )

@Preview
@Composable
private fun PreviewSeedLight() {
    TheColorTheme {
        Surface(color = Color(0xFF_126B40)) {
            ProvideColorsOnTintedSurface(colorsOnDarkSurface()) {
                PreviewContentSeed()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewSeedDark() {
    TheColorTheme {
        Surface(color = Color(0xFF_F0F8FF)) {
            ProvideColorsOnTintedSurface(colorsOnLightSurface()) {
                PreviewContentSeed()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewExactLight() {
    TheColorTheme {
        Surface(color = Color(0xFF_126B40)) {
            ProvideColorsOnTintedSurface(colorsOnDarkSurface()) {
                PreviewContentExact()
            }
        }
    }
}

@Preview
@Composable
private fun PreviewExactDark() {
    TheColorTheme {
        Surface(color = Color(0xFF_F0F8FF)) {
            ProvideColorsOnTintedSurface(colorsOnLightSurface()) {
                PreviewContentExact()
            }
        }
    }
}

@Composable
private fun PreviewContentSeed() {
    ColorSpecs(
        colorName = "Jewel",
        exactMatch = ColorDetailsData.ExactMatch.No(
            exactValue = "#126B40",
            exactColor = ColorInt(0x126B40),
            deviation = "1366",
        ),
        colorRoleData = ColorDetailsData.ColorRoleData.Seed(
            exactColor = ColorInt(0x126B40),
            goToExactColor = {},
        ),
        strings = ColorDetailsUiStrings(
            hexLabel = "_",
            rgbLabel = "_",
            hslLabel = "_",
            hsvLabel = "_",
            cmykLabel = "_",
            nameLabel = "NAME",
            exactMatchLabel = "EXACT MATCH",
            exactMatchYes = "Yes",
            exactMatchNo = "No",
            goBackToSeedColorButtonText = "Go back to",
            exactValueLabel = "EXACT VALUE",
            deviationLabel = "DEVIATION",
        ),
    )
}

@Composable
private fun PreviewContentExact() {
    ColorSpecs(
        colorName = "Jewel",
        exactMatch = ColorDetailsData.ExactMatch.Yes,
        colorRoleData = ColorDetailsData.ColorRoleData.Exact(
            seedColor = ColorInt(0x1A803F),
            goToSeedColor = {},
        ),
        strings = ColorDetailsUiStrings(
            hexLabel = "_",
            rgbLabel = "_",
            hslLabel = "_",
            hsvLabel = "_",
            cmykLabel = "_",
            nameLabel = "NAME",
            exactMatchLabel = "EXACT MATCH",
            exactMatchYes = "Yes",
            exactMatchNo = "No",
            goBackToSeedColorButtonText = "Go back to",
            exactValueLabel = "EXACT VALUE",
            deviationLabel = "DEVIATION",
        ),
    )
}