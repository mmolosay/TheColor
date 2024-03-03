package io.github.mmolosay.thecolor.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@Composable
fun HomeScreen(
    vm: HomeViewModel,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: @Composable () -> Unit,
) {
    val data = vm.dataFlow.collectAsStateWithLifecycle().value
    val viewData = rememberViewData()
    val uiData = HomeUiData(data, viewData)
    HomeScreen(
        uiData = uiData,
        colorInput = colorInput,
        colorPreview = colorPreview,
        colorCenter = colorCenter,
    )
}

@Composable
fun HomeScreen(
    uiData: HomeUiData,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.height(160.dp))
        Text(
            text = uiData.headline,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))
        colorInput()

        ProceedButton(uiData.proceedButton)

        Spacer(modifier = Modifier.height(8.dp))
        colorPreview()

        Spacer(modifier = Modifier.height(16.dp)) // minimum
        Spacer(modifier = Modifier.weight(1f)) // maximum
        colorCenter()
    }
}

@Composable
private fun ProceedButton(
    uiData: HomeUiData.ProceedButton,
) {
    Button(
        onClick = uiData.onClick,
        enabled = uiData.enabled,
    ) {
        Text(text = uiData.text)
    }
}

@Composable
private fun rememberViewData(): HomeUiData.ViewData {
    val context = LocalContext.current
    return remember { HomeViewData(context) }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        HomeScreen(
            uiData = previewUiData(),
            colorInput = {
                Text(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .fillMaxWidth()
                        .height(100.dp)
                        .wrapContentSize(),
                    text = "Color Input",
                )
            },
            colorPreview = {
                Text(
                    modifier = Modifier
                        .background(Color.LightGray),
                    text = "Color Preview",
                )
            },
            colorCenter = {
                Text(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .fillMaxWidth()
                        .height(300.dp)
                        .wrapContentSize(),
                    text = "Color Input",
                )
            },
        )
    }
}

private fun previewUiData() =
    HomeUiData(
        headline = "Find your color",
        proceedButton = HomeUiData.ProceedButton(
            onClick = {},
            enabled = true,
            text = "Proceed",
        ),
    )