package io.github.mmolosay.thecolor.demo.presentation.feature.editor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.demo.presentation.feature.cups.CupDial
import io.github.mmolosay.thecolor.demo.presentation.feature.grams.GramsInput
import io.github.mmolosay.thecolor.demo.presentation.feature.ounces.OuncesInput

@Composable
fun AmountEditor(
    feature: AmountEditorFeature,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        GramsInput(feature = feature.grams)
        OuncesInput(feature = feature.ounces)
        CupDial(feature = feature.cups)
    }
}
