package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.LineHeightStyle
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.center.ColorCenterUiData.Page.ChangePageButton
import io.github.mmolosay.thecolor.presentation.design.colorsOnTintedSurface

@Composable
fun DetailsPage(
    uiData: ColorCenterUiData.Page,
    colorDetails: @Composable () -> Unit,
) =
    Page(
        content = colorDetails,
        changePageButton = {
            ChangePageButton(
                uiData = uiData.changePageButton,
            )
        },
    )

@Composable
fun SchemePage(
    uiData: ColorCenterUiData.Page,
    colorScheme: @Composable () -> Unit,
) =
    Page(
        content = colorScheme,
        changePageButton = {
            ChangePageButton(
                uiData = uiData.changePageButton,
            )
        },
    )

@Composable
internal fun Page(
    content: @Composable () -> Unit,
    changePageButton: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        content()
        Spacer(modifier = Modifier.height(16.dp)) // bounded min height
        Spacer(modifier = Modifier.weight(1f)) // unbounded max height
        changePageButton()
    }
}

@Composable
private fun ChangePageButton(
    uiData: ChangePageButton,
) {
    @Composable
    fun Icon() =
        androidx.compose.material3.Icon(
            imageVector = uiData.icon,
            contentDescription = null, // described by neighboring text
        )

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
        if (uiData.iconPlacement == ChangePageButton.IconPlacement.Leading) {
            Icon()
        }
        Text(
            text = uiData.text,
            style = LocalTextStyle.current.merge(addedTextStyle),
        )
        if (uiData.iconPlacement == ChangePageButton.IconPlacement.Trailing) {
            Icon()
        }
    }
}