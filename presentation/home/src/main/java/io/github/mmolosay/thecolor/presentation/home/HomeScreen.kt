package io.github.mmolosay.thecolor.presentation.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterShape
import io.github.mmolosay.thecolor.presentation.design.ChangeNavigationBarAsSideEffect
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.RestoreNavigationBarAsSideEffect
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.home.HomeUiData.ProceedButton
import io.github.mmolosay.thecolor.presentation.home.HomeUiData.ShowColorCenter
import io.github.mmolosay.thecolor.presentation.input.ColorInput
import io.github.mmolosay.thecolor.presentation.preview.ColorPreview

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSettings: () -> Unit,
) {
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val viewData = rememberViewData()
    val uiData = HomeUiData(data, viewData)

    HomeScreen(
        uiData = uiData,
        colorInput = {
            ColorInput(
                vm = viewModel.colorInputViewModel,
                hexViewModel = viewModel.colorInputViewModel.hexViewModel,
                rgbViewModel = viewModel.colorInputViewModel.rgbViewModel,
            )
        },
        colorPreview = {
            ColorPreview(
                vm = viewModel.colorPreviewViewModel,
            )
        },
        colorCenter = {
            ColorCenter(
                vm = viewModel.colorCenterViewModel,
                detailsViewModel = viewModel.colorCenterViewModel.colorDetailsViewModel,
                schemeViewModel = viewModel.colorCenterViewModel.colorSchemeViewModel,
                modifier = Modifier.padding(top = 24.dp),
            )
        },
        navigateToSettings = navigateToSettings,
    )
}

@Composable
fun HomeScreen(
    uiData: HomeUiData,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: @Composable () -> Unit,
    navigateToSettings: () -> Unit,
) {
    Scaffold { contentPadding ->
        Home(
            uiData = uiData,
            colorInput = colorInput,
            colorPreview = colorPreview,
            colorCenter = colorCenter,
            navigateToSettings = navigateToSettings,
            modifier = Modifier.padding(contentPadding),
        )
    }
}

@Composable
fun Home(
    uiData: HomeUiData,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: @Composable () -> Unit,
    navigateToSettings: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState()),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopBar(uiData = uiData.topBar)

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
        ColorCenterOnTintedSurface(
            state = uiData.showColorCenter,
            colorCenter = colorCenter,
        )
    }

    LaunchedEffect(uiData.navEvent) {
        val event = uiData.navEvent ?: return@LaunchedEffect
        when (event) {
            is HomeData.NavEvent.GoToSettings -> navigateToSettings()
        }
        event.onConsumed()
    }
}

@Composable
private fun ProceedButton(
    uiData: ProceedButton,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val wrappedOnClick: () -> Unit = remember(uiData) {
        {
            uiData.onClick()
            keyboardController?.hide()
        }
    }
    Button(
        onClick = wrappedOnClick,
        enabled = uiData.enabled,
    ) {
        Text(text = uiData.text)
    }
}

@Composable
private fun ColorCenterOnTintedSurface(
    state: ShowColorCenter,
    colorCenter: @Composable () -> Unit,
) {
    RestoreNavigationBarAsSideEffect()
    if (state !is ShowColorCenter.Yes) return

    val colors = rememberContentColors(useLight = state.useLightContentColors)
    ProvideColorsOnTintedSurface(colors) {
        Box(
            modifier = Modifier
                .graphicsLayer {
                    clip = true
                    shape = ColorCenterShape
                }
                .background(state.backgroundColor)
        ) {
            colorCenter()
        }
    }
    ChangeNavigationBarAsSideEffect(
        navigationBarColor = state.backgroundColor,
        isAppearanceLightNavigationBars = !state.useLightContentColors,
    )
}

@Composable
private fun TopBar(
    uiData: HomeUiData.TopBar,
) {
    val onClick = uiData.settingsAction.onClick
    val debouncedOnClick: () -> Unit = remember(onClick) {
        debounced(action = onClick)
    }
    Row(
        horizontalArrangement = Arrangement.End,
        modifier = Modifier.fillMaxWidth(),
    ) {
        IconButton(onClick = debouncedOnClick) {
            Icon(
                imageVector = Icons.Rounded.Settings,
                contentDescription = uiData.settingsAction.iconContentDescription,
            )
        }
    }
}

@Composable
private fun rememberViewData(): HomeUiData.ViewData {
    val context = LocalContext.current
    return remember { HomeViewData(context) }
}

@Composable
private fun rememberContentColors(useLight: Boolean): ColorsOnTintedSurface =
    remember(useLight) {
        if (useLight) colorsOnDarkSurface() else colorsOnLightSurface()
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
            navigateToSettings = {},
        )
    }
}

private fun previewUiData() =
    HomeUiData(
        topBar = HomeUiData.TopBar(
            settingsAction = HomeUiData.TopBar.SettingsAction(
                onClick = {},
                iconContentDescription = "",
            ),
        ),
        headline = "Find your color",
        proceedButton = ProceedButton(
            onClick = {},
            enabled = true,
            text = "Proceed",
        ),
        showColorCenter = ShowColorCenter.Yes(
            backgroundColor = Color(0xFF_123456),
            useLightContentColors = true,
        ),
        navEvent = null,
    )