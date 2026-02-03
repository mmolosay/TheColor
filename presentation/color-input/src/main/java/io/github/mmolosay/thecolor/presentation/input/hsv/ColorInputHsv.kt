package io.github.mmolosay.thecolor.presentation.input.hsv

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.input.model.DataState
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.domain.model.Color as DomainColor

@Composable
fun ColorInputHsv(
    viewModel: ColorInputHsvViewModel,
) {
    val dataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
    when (dataState) {
        is DataState.BeingInitialized ->
            doNothing() // TODO: add loading as in other 'Color Input' types
        is DataState.Ready ->
            ColorInputHsv(
                data = dataState.data
            )
    }
}

@Composable
fun ColorInputHsv(
    data: ColorInputHsvData,
) {
    val hue = run {
        if (data.color != null) HueValue(data.color)
        else HueValue.Min
    }
    val sv = run {
        if (data.color != null) SaturationAndValue(data.color)
        else SaturationAndValue(saturation = 1f, value = 1f)
    }
    fun HsvColor(hue: HueValue, sv: SaturationAndValue): DomainColor.Hsv =
        DomainColor.Hsv(
            hue = hue.value,
            saturation = sv.saturation,
            value = sv.value,
        )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
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
                data.onColorChanged(newColor)
            },
        )

        Spacer(Modifier.width(8.dp))
        HuePicker2(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            hue = hue,
            onChange = { newHue ->
                val newColor = HsvColor(newHue, sv)
                data.onColorChanged(newColor)
            },
            hueBarShape = RoundedCornerShape(size = 4.dp),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputHsv(
            data = previewData(),
        )
    }
}

private fun previewData() =
    ColorInputHsvData(
        color = DomainColor.Hsv(hue = 259f, saturation = 0.65f, value = 0.82f),
        onColorChanged = {},
    )