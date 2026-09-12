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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.hsv.HsvColorUtils.HsvHueRange
import io.github.mmolosay.thecolor.presentation.input.hsv.HsvColorUtils.HsvSaturationRange
import io.github.mmolosay.thecolor.presentation.input.hsv.HsvColorUtils.HsvValueRange
import kotlinx.coroutines.Job
import io.github.mmolosay.thecolor.domain.color.Color as DomainColor

@Composable
fun rememberColorInputHsvFacade(handle: ColorInputHsvHandle): ColorInputHsvFacade {
    val data = handle.dataFlow.collectAsStateWithLifecycle().value
    return remember(handle, data) { handle.facade(data) }
}

@Composable
fun ColorInputHsv(
    facade: ColorInputHsvFacade,
) {
    val execute by rememberUpdatedState(facade.execute) // stable across recompositions
    val hue = run {
        val color = facade.color
        if (color != null) HueValue(color)
        else HueValue(HsvHueRange.start)
    }
    val sv = run {
        val color = facade.color
        if (color != null) SaturationAndValue(color)
        else SaturationAndValue(saturation = HsvSaturationRange.endInclusive, value = HsvValueRange.endInclusive)
    }
    fun HsvColor(hue: HueValue, sv: SaturationAndValue): DomainColor.Hsv =
        DomainColor.Hsv(
            hue = hue.value,
            saturation = sv.saturation,
            value = sv.value,
        )

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
                val newColor = HsvColor(hue, newSv)
                val action = ColorInputHsvAction.SetColor(newColor)
                execute(action)
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
                    val newColor = HsvColor(newHue, sv)
                    val action = ColorInputHsvAction.SetColor(newColor)
                    execute(action)
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
            ColorInputHsv(
                facade = previewFacade(),
            )
        }
    }
}

private fun previewFacade() =
    ColorInputHsvFacade(
        color = DomainColor.Hsv(hue = 117f, saturation = 0.59f, value = 0.31f),
        execute = { Job() },
    )