package io.github.mmolosay.thecolor.presentation.input.picker

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.skydoves.colorpicker.compose.BrightnessSlider
import com.github.skydoves.colorpicker.compose.HsvColorPicker
import com.github.skydoves.colorpicker.compose.rememberColorPickerController
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@Composable
fun ColorInputPicker() {
    Column {
        SaturationAndValuePicker(
            modifier = Modifier
                .fillMaxWidth()
                .height(128.dp)
                .clip(RoundedCornerShape(size = 4.dp)),
            hue = HueSource.FromColor(Color(0xFF859A8A)),
            saturationAndValue = SaturationAndValue(saturation = 1f, value = 1f),
            onChange = {},
        )
    }
}

@Composable
private fun LibraryPicker() {
    val controller = rememberColorPickerController()
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        HsvColorPicker(
            modifier = Modifier
                .size(128.dp),
            controller = controller,
            onColorChanged = { },
            initialColor = Color.Black,
        )

        Spacer(Modifier.height(12.dp))
        BrightnessSlider(
            modifier = Modifier
                .fillMaxWidth()
                .height(32.dp),
            controller = controller,
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