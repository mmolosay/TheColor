package io.github.mmolosay.thecolor.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceStack
import io.github.mmolosay.thecolor.presentation.api.NoopNavBarAppearanceStack
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterShape
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.home.HomeUiData.ProceedButton
import io.github.mmolosay.thecolor.presentation.home.HomeUiData.ShowColorCenter
import io.github.mmolosay.thecolor.presentation.impl.TintedSurface
import io.github.mmolosay.thecolor.presentation.impl.toDpOffset
import io.github.mmolosay.thecolor.presentation.impl.toDpSize
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInput
import io.github.mmolosay.thecolor.presentation.preview.ColorPreview

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSettings: () -> Unit,
    navBarAppearanceStack: NavBarAppearanceStack,
) {
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val viewData = rememberViewData()
    val uiData = HomeUiData(data, viewData)
    val navEvent = viewModel.navEventFlow.collectAsStateWithLifecycle().value

    HomeScreen(
        uiData = uiData,
        navEvent = navEvent,
        colorInput = {
            ColorInput(
                viewModel = viewModel.colorInputViewModel,
            )
        },
        colorPreview = {
            ColorPreview(
                viewModel = viewModel.colorPreviewViewModel,
            )
        },
        colorCenter = {
            ColorCenter(
                modifier = Modifier.padding(top = 24.dp),
                viewModel = viewModel.colorCenterViewModel,
                navBarAppearanceStack = navBarAppearanceStack,
            )
        },
        navigateToSettings = navigateToSettings,
        navBarAppearanceStack = navBarAppearanceStack,
    )
}

@Composable
fun HomeScreen(
    uiData: HomeUiData,
    navEvent: HomeNavEvent?,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: @Composable () -> Unit,
    navigateToSettings: () -> Unit,
    navBarAppearanceStack: NavBarAppearanceStack,
) {
    Scaffold { contentPadding ->
        Home(
            modifier = Modifier.padding(contentPadding),
            uiData = uiData,
            navEvent = navEvent,
            colorInput = colorInput,
            colorPreview = colorPreview,
            colorCenter = colorCenter,
            navigateToSettings = navigateToSettings,
            navBarAppearanceStack = navBarAppearanceStack,
        )
    }
}

@Composable
fun Home(
    uiData: HomeUiData,
    navEvent: HomeNavEvent?,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: @Composable () -> Unit,
    navigateToSettings: () -> Unit,
    navBarAppearanceStack: NavBarAppearanceStack,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    var positionInRoot by remember { mutableStateOf<DpOffset?>(null) }
    var size by remember { mutableStateOf<DpSize?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .onGloballyPositioned { coordinates ->
                positionInRoot = coordinates
                    .positionInRoot()
                    .toDpOffset(density)
                size = coordinates.size.toDpSize(density)
            },
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
        // TODO: animated color preview is a part of bigger animation, see AnimatedColorCenter.kt
//        AnimatedColorPreview(
//            colorPreview = colorPreview,
//            state = uiData.colorPreviewState,
//            containerSize = size,
//            containerPositionInRoot = positionInRoot,
//        )
        colorPreview()

        Spacer(modifier = Modifier.height(16.dp)) // minimum
        Spacer(modifier = Modifier.weight(1f)) // maximum
//        AnimatedColorCenter {
        ColorCenterOnTintedSurface(
            state = uiData.showColorCenter,
            colorCenter = colorCenter,
            navBarAppearanceStack = navBarAppearanceStack,
        )
//        }
    }

    LaunchedEffect(navEvent) {
        val event = navEvent ?: return@LaunchedEffect
        when (event) {
            is HomeNavEvent.GoToSettings -> navigateToSettings()
        }
        event.onConsumed()
    }
    LaunchedEffect(uiData) {
        val toastData = uiData.invalidSubmittedColorToast ?: return@LaunchedEffect
        Toast
            .makeText(context, toastData.message, Toast.LENGTH_SHORT)
            .show()
        toastData.onShown()
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
    navBarAppearanceStack: NavBarAppearanceStack,
) {
    if (state !is ShowColorCenter.Yes) return
    val colors = if (state.useLightContentColors) colorsOnDarkSurface() else colorsOnLightSurface()
    TintedSurface(
        modifier = Modifier
            .graphicsLayer {
                clip = true
                shape = ColorCenterShape
            },
        surfaceColor = state.backgroundColor,
        contentColors = colors,
    ) {
        colorCenter()
    }
    DisposableEffect(state) {
        val appearance = NavBarAppearance(
            color = state.navigationBarColor,
            isLight = state.isNavigationBarLight,
        )
        navBarAppearanceStack.push(appearance)
        onDispose {
            navBarAppearanceStack.peel()
        }
    }
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

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        HomeScreen(
            uiData = previewUiData(),
            navEvent = null,
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
            navBarAppearanceStack = NoopNavBarAppearanceStack,
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
        colorPreviewState = HomeUiData.ColorPreviewState.Default,
        showColorCenter = ShowColorCenter.Yes(
            backgroundColor = Color(0xFF_123456),
            useLightContentColors = true,
            navigationBarColor = ColorInt(0x123456),
            isNavigationBarLight = true,
        ),
        invalidSubmittedColorToast = null,
    )