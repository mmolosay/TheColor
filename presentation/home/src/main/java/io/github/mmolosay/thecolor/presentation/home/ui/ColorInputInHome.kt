package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroup
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroupFeature
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHex
import io.github.mmolosay.thecolor.presentation.input.hex.ColorInputHexFeature
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsv
import io.github.mmolosay.thecolor.presentation.input.hsv.ColorInputHsvFeature
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgb
import io.github.mmolosay.thecolor.presentation.input.rgb.ColorInputRgbFeature

/**
 * The "bare" 'Color Input' [Composable], free of any 'Home'-specific logic.
 */
internal typealias BareColorInputComposable =
        @Composable (Modifier) -> Unit

@Composable
internal fun BareColorInput(
    group: ColorInputGroupFeature,
    hex: ColorInputHexFeature,
    rgb: ColorInputRgbFeature,
    hsv: ColorInputHsvFeature,
    modifier: Modifier = Modifier,
) {
    ColorInputGroup(
        modifier = modifier,
        feature = group,
        hexInput = {
            ColorInputHex(feature = hex)
        },
        rgbInput = {
            ColorInputRgb(feature = rgb)
        },
        hsvInput = {
            ColorInputHsv(feature = hsv)
        },
    )
}

/**
 * 'Color Input' as the 'Home' feature presents it.
 */
@Composable
internal fun ColorInputInHome(
    content: BareColorInputComposable,
) {
    val modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp)
    content(modifier)
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            ColorInputInHome { modifier ->
                Placeholder(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(120.dp),
                ) {
                    Text("Bare Color Input")
                }
            }
        }
    }
}