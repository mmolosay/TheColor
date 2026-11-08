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
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import kotlinx.coroutines.Job
import io.github.mmolosay.thecolor.domain.color.Color as DomainColor

@Composable
fun ColorInputHsv(
    facade: ColorInputHsvFacade,
) {
    val execute by rememberUpdatedState(facade.execute) // stable across recompositions
    val color = facade.color
    val hue = HueValue(color)
    val sv = SaturationAndValue(color)

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
                val action = ColorInputHsvAction.SetSaturationAndValue(newSv.saturation, newSv.value)
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
                    val action = ColorInputHsvAction.SetHue(newHue.value)
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