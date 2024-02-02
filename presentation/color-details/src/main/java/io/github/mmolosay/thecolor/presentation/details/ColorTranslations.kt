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
import androidx.compose.ui.unit.dp
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
    }

@Composable
private fun Hex(uiData: ColorTranslation.Hex) =
    Row {
        Label(
            text = uiData.label,
        )
        SpacerBetweenLabelAndValue()
        Value(
            text = uiData.value,
        )
    }

@Composable
private fun Rgb(uiData: ColorTranslation.Rgb) =
    Row {
        Label(
            text = uiData.label,
        )

        SpacerBetweenLabelAndValue()
        RowOfValues {
            Value(
                text = uiData.r,
            )
            Value(
                text = uiData.g,
            )
            Value(
                text = uiData.b,
            )
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

@Composable
private fun SpacerBetweenLabelAndValue() =
    Spacer(modifier = Modifier.width(12.dp))
