package io.github.mmolosay.thecolor.presentation.home

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.nav.bar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.nav.bar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterShape
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.home.HomeUiData.ProceedButton
import io.github.mmolosay.thecolor.presentation.home.HomeUiData.ShowColorCenter
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeNavEvent
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent
import io.github.mmolosay.thecolor.presentation.impl.TintedSurface
import io.github.mmolosay.thecolor.presentation.impl.onlyBottom
import io.github.mmolosay.thecolor.presentation.impl.toDpOffset
import io.github.mmolosay.thecolor.presentation.impl.toDpSize
import io.github.mmolosay.thecolor.presentation.impl.toLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.impl.withoutBottom
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInput
import io.github.mmolosay.thecolor.presentation.preview.ColorPreview
import io.github.mmolosay.thecolor.utils.doNothing

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSettings: () -> Unit,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val context = LocalContext.current
    val strings = remember(context) { HomeUiStrings(context) }
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val uiData = HomeUiData(data, strings)
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
        colorCenter = ColorCenter@{
            val viewModel = viewModel.colorCenterViewModelFlow
                .collectAsStateWithLifecycle().value ?: return@ColorCenter
            val childController = remember(navBarAppearanceController) {
                navBarAppearanceController.branch("Color Center")
            }
            ColorCenter(
                modifier = Modifier.padding(top = 24.dp),
                viewModel = viewModel,
                navBarAppearanceController = childController,
            )
        },
        navigateToSettings = navigateToSettings,
        navBarAppearanceController = navBarAppearanceController,
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
    navBarAppearanceController: NavBarAppearanceController,
) {
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.withoutBottom(),
    ) { contentPadding ->
        Home(
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding), // ensures correct height of 'TopAppBar()'
            uiData = uiData,
            navEvent = navEvent,
            colorInput = colorInput,
            colorPreview = colorPreview,
            colorCenter = colorCenter,
            navigateToSettings = navigateToSettings,
            navBarAppearanceController = navBarAppearanceController,
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
    navBarAppearanceController: NavBarAppearanceController,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current
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
        TopBar(
            uiData = uiData.topBar,
        )

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
            navBarAppearanceController = navBarAppearanceController,
        )
//        }
    }

    LaunchedEffect(navEvent) {
        val event = navEvent ?: return@LaunchedEffect
        when (event) {
            is HomeNavEvent.GoToSettings -> {
                focusManager.clearFocus()
                navigateToSettings()
            }
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
    navBarAppearanceController: NavBarAppearanceController,
) {
    if (state !is ShowColorCenter.Yes) return
    val colors = if (state.useLightContentColors) colorsOnDarkSurface() else colorsOnLightSurface()
    val windowInsets = WindowInsets.systemBars.onlyBottom()
    TintedSurface(
        modifier = Modifier
            .graphicsLayer {
                clip = true
                shape = ColorCenterShape
            },
        surfaceColor = state.backgroundColor,
        contentColors = colors,
    ) {
        Box(
            modifier = Modifier
                .padding(windowInsets.asPaddingValues())
                .consumeWindowInsets(windowInsets),
        ) {
            colorCenter()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    DisposableEffect(lifecycleOwner, state) {
        val observer = ColorCenterLifecycleObserver(
            navBarAppearanceController = navBarAppearanceController,
            appearance = state.navBarAppearance,
        ).toLifecycleEventObserver()
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            navBarAppearanceController.clear()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    uiData: HomeUiData.TopBar,
) {
    val onClick = uiData.settingsAction.onClick
    val debouncedOnClick: () -> Unit = remember(onClick) {
        debounced(action = onClick)
    }
    TopAppBar(
        title = {},
        actions = {
            IconButton(onClick = debouncedOnClick) {
                Icon(
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = uiData.settingsAction.iconContentDescription,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

private class ColorCenterLifecycleObserver(
    private val navBarAppearanceController: NavBarAppearanceController,
    private val appearance: NavBarAppearance,
) : ExtendedLifecycleEventObserver {

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
        directionChange: LifecycleDirectionChangeEvent?,
    ) {
        when (directionChange) {
            LifecycleDirectionChangeEvent.EnteringForeground -> {
                navBarAppearanceController.push(appearance)
            }
            LifecycleDirectionChangeEvent.LeavingForeground -> {
                navBarAppearanceController.clear()
            }
            null -> doNothing()
        }
    }
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
            navBarAppearanceController = remember { RootNavBarAppearanceController() },
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
            navBarAppearance = navBarAppearance(
                useLightTintForControls = true,
            ),
        ),
        invalidSubmittedColorToast = null,
    )