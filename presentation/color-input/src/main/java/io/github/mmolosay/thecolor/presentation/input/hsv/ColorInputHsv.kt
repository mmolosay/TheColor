package io.github.mmolosay.thecolor.presentation.input.hsv

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalMinimumInteractiveComponentSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import io.github.mmolosay.thecolor.domain.color.Color as DomainColor

@Composable
fun ColorInputHsv(
    feature: ColorInputHsvFeature,
) =
    ColorInputHsv(
        viewData = feature.viewData,
        onHueChange = { hue ->
            feature.pick { current -> current.copy(hue = hue) }
        },
        onSaturationAndValueChange = { saturation, value ->
            feature.pick { current -> current.copy(saturation = saturation, value = value) }
        },
    )

@Composable
fun ColorInputHsv(
    viewData: StateFlow<ColorInputHsvViewData>,
    onHueChange: (hue: Float) -> Unit,
    onSaturationAndValueChange: (saturation: Float, value: Float) -> Unit,
) {
    val data = viewData.collectAsStateWithLifecycle().value
    val hue = HueValue(data.color)
    val sv = SaturationAndValue(data.color)

    Row(
        modifier = Modifier.height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Center,
    ) {
        SaturationAndValuePicker(
            modifier = Modifier
                .width(128.dp)
                .aspectRatio(1f / 1f)
                .clip(RoundedCornerShape(size = 4.dp)),
            hue = hue,
            sv = sv,
            onChange = { newSv ->
                onSaturationAndValueChange(newSv.saturation, newSv.value)
            },
        )

        Spacer(Modifier.width(8.dp))
        CompositionLocalProvider(
            LocalMinimumInteractiveComponentSize provides 0.dp,
        ) {
            HuePicker(
                modifier = Modifier
                    .fillMaxHeight(),
                hue = hue,
                onChange = { newHue ->
                    onHueChange(newHue.value)
                },
            )
        }
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface(color = MaterialTheme.colorScheme.background) {
            val viewData = remember {
                val value = ColorInputHsvViewData(
                    color = DomainColor.Hsv(hue = 117f, saturation = 0.59f, value = 0.31f),
                )
                MutableStateFlow(value)
            }
            ColorInputHsv(
                viewData = viewData,
                onHueChange = { hue ->
                    viewData.update {
                        it.copy(
                            color = it.color.copy(hue = hue),
                        )
                    }
                },
                onSaturationAndValueChange = { saturation, value ->
                    viewData.update {
                        it.copy(
                            color = it.color.copy(saturation = saturation, value = value),
                        )
                    }
                },
            )
        }
    }
}