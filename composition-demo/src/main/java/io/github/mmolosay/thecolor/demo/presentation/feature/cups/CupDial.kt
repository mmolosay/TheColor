package io.github.mmolosay.thecolor.demo.presentation.feature.cups

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import io.github.mmolosay.thecolor.demo.R
import io.github.mmolosay.thecolor.presentation.common.composition.collectAsViewData

@Composable
fun CupDial(
    dial: CupDialFeature,
    modifier: Modifier = Modifier,
) {
    val viewData by dial.viewData.collectAsViewData()
    Column(
        modifier = modifier,
    ) {
        val cups = viewData.cups
        Text(
            text = if (cups != null) {
                stringResource(R.string.cups_dial_amount, cups)
            } else {
                stringResource(R.string.cups_dial_label)
            },
        )
        Slider(
            value = viewData.position,
            onValueChange = { position -> dial.drag(position) },
            valueRange = 0f..MaxCups,
        )
    }
}
