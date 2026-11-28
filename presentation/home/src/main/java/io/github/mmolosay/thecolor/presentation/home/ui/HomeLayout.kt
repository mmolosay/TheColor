package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.LayoutCoordinates
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.unit.dp

@Composable
internal fun HomeLayout(
    modifier: Modifier = Modifier,
    scrollState: ScrollState,
    onGloballyPositioned: (LayoutCoordinates) -> Unit,
    topBar: @Composable () -> Unit,
    headline: @Composable () -> Unit,
    buttonSection: @Composable () -> Unit,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: @Composable () -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .onGloballyPositioned(onGloballyPositioned),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        topBar()

        Spacer(modifier = Modifier.height(160.dp))
        headline()

        Spacer(modifier = Modifier.height(16.dp))
        colorInput()

        Spacer(modifier = Modifier.height(8.dp))
        buttonSection()

        Spacer(modifier = Modifier.height(8.dp))
        colorPreview()
        colorCenter()
    }
}

