package io.github.mmolosay.thecolor.presentation.home.details

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LocalMinimumInteractiveComponentEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.em
import io.github.mmolosay.thecolor.presentation.home.R
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ColorSpec
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
internal fun ColorSpecs(
    specs: List<ColorSpec>,
    modifier: Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        specs.forEach { spec ->
            when (spec) {
                is ColorSpec.Name -> Name(spec)
                is ColorSpec.ExactMatch -> ExactMatch(spec)
                is ColorSpec.ExactValue -> ExactValue(spec)
                is ColorSpec.Deviation -> Deviation(spec)
            }
        }
    }
}

@Composable
private fun Name(uiData: ColorSpec.Name) {
    Column {
        Label(
            text = uiData.label,
            color = uiData.labelColor,
        )
        Value(
            text = uiData.value,
            color = uiData.valueColor,
        )
    }
}

@Composable
private fun ExactMatch(uiData: ColorSpec.ExactMatch) {
    Column {
        Label(
            text = uiData.label,
            color = uiData.labelColor,
        )
        Value(
            text = uiData.value,
            color = uiData.valueColor,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExactValue(uiData: ColorSpec.ExactValue) {
    Column {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Label(
                text = uiData.label,
                color = uiData.labelColor,
            )
            CompositionLocalProvider(
                LocalMinimumInteractiveComponentEnforcement provides false,
            ) {
                val colors = IconButtonDefaults.iconButtonColors(
                    contentColor = uiData.iconColor,
                )
                IconButton(
                    onClick = uiData.onClick,
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
            Value(
                text = uiData.value,
                color = uiData.valueColor,
            )
            ColorPreview(color = uiData.exactColor)
        }
    }
}

@Composable
private fun ColorPreview(color: Color) =
    Box(
        modifier = Modifier
            .size(13.dp)
            .clip(CircleShape)
            .background(color)
    )

@Composable
private fun Deviation(uiData: ColorSpec.Deviation) {
    Column {
        Label(
            text = uiData.label,
            color = uiData.labelColor,
        )
        Value(
            text = uiData.value,
            color = uiData.valueColor,
        )
    }
}

@Composable
private fun Label(
    text: String,
    color: Color,
) =
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.labelSmall.copy(
            letterSpacing = 0.1666.em,
        )
    )

@Composable
private fun Value(
    text: String,
    color: Color,
) =
    Text(
        text = text,
        color = color,
        style = MaterialTheme.typography.bodyLarge,
    )