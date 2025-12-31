package io.github.mmolosay.thecolor.presentation.input.picker

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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@Composable
fun ColorInputPicker() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        horizontalArrangement = Arrangement.Center,
    ) {
        var hue by remember { mutableStateOf(HueValue.Min) }
        var sv by remember {
            val value = SaturationAndValue(saturation = 1f, value = 1f)
            mutableStateOf(value)
        }
        LaunchedEffect(hue, sv) {
            println("PRIVET, new HSV: hue=$hue, saturation=${sv.saturation}, value=${sv.value}")
        }
        SaturationAndValuePicker(
            modifier = Modifier
                .height(128.dp)
                .aspectRatio(1f / 1f)
                .clip(RoundedCornerShape(size = 4.dp)),
            hue = hue,
            sv = sv,
            onChange = { newSv ->
                sv = newSv
            },
        )

        Spacer(Modifier.width(8.dp))
        HuePicker2(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
            hue = HueValue.Min,
            onChange = { hue = it },
            hueBarShape = RoundedCornerShape(size = 4.dp),
        )
    }
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        ColorInputPicker()
    }
}