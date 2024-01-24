package io.github.mmolosay.thecolor.presentation.home.details

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ColorTranslation
import io.github.mmolosay.thecolor.presentation.home.details.ColorDetailsUiData.ColorTranslations

@Composable
internal fun ColorTranslations(uiData: ColorTranslations) =
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Hex(uiData.hex)
        Rgb(uiData.rgb)
    }

@Composable
private fun Hex(uiData: ColorTranslation.Hex) =
    Row {
        Label(
            text = uiData.label,
            color = uiData.labelColor,
        )
        SpacerBetweenLabelAndValue()
        Value(
            text = uiData.value,
            color = uiData.valueColor,
        )
    }

@Composable
private fun Rgb(uiData: ColorTranslation.Rgb) =
    Row {
        Text(
            text = uiData.label,
            color = uiData.labelColor,
        )

        SpacerBetweenLabelAndValue()
        Value(
            text = uiData.r,
            color = uiData.valueColor,
        )

        SpacerBetweenValues()
        Value(
            text = uiData.g,
            color = uiData.valueColor,
        )

        SpacerBetweenValues()
        Value(
            text = uiData.b,
            color = uiData.valueColor,
        )
    }

@Composable
private fun Label(
    text: String,
    color: Color,
) =
    Text(
        text = text,
        color = color,
    )

@Composable
private fun SpacerBetweenLabelAndValue() =
    Spacer(modifier = Modifier.width(12.dp))

@Composable
private fun SpacerBetweenValues() =
    Spacer(modifier = Modifier.width(8.dp))

@Composable
private fun Value(
    text: String,
    color: Color,
) =
    Text(
        text = text,
        color = color,
    )