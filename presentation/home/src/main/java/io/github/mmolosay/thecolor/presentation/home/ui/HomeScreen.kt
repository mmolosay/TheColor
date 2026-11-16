package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.PlaceholderDefaults
import io.github.mmolosay.thecolor.presentation.common.compose.withoutBottom
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.details.ui.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ui.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewData
import io.github.mmolosay.thecolor.presentation.home.ui.center.BareColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.center.BareColorCenterComposable
import io.github.mmolosay.thecolor.presentation.home.ui.center.ColorCenterInHome
import io.github.mmolosay.thecolor.presentation.home.ui.preview.BareColorPreviewComposable
import io.github.mmolosay.thecolor.presentation.home.ui.preview.ColorPreviewInHome
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.preview.AnimatedColorPreview
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.utils.mapState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onSettingsClick: () -> Unit,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val context = LocalContext.current
    val strings = remember(context) { HomeUiStrings(context) }

    // TODO: HomeViewModel may take longer time to initialize 'viewData' on laggy devices.
    //       Implement 'loading' state and cross-fade?
    val data = viewModel.viewData.collectAsStateWithLifecycle().value ?: return

    val flowOfUiState = remember {
        viewModel.viewData.mapState { requireNotNull(it).toUiState() }
    }
    val animController = remember {
        val animState = flowOfUiState.value.toAnimState()
            .let { requireNotNull(it) } // assume the initial state is always valid
        HomeAnimController(animState)
    }
    LaunchedEffect(Unit) {
        flowOfUiState.collect { uiState ->
            val finish = uiState.toAnimState() ?: return@collect
            val destStates = animController.makeDestStates(finish = finish)
            if (destStates != null) {
                animController.run(destStates)
            }
        }
    }

    val colorInput: BareColorInputComposable = { modifier ->
        BareColorInput(
            modifier = modifier,
            group = viewModel.colorInputGroup,
            hex = viewModel.colorEditor.hex,
            rgb = viewModel.colorEditor.rgb,
            hsv = viewModel.colorEditor.hsv,
        )
    }
    val colorPreview: BareColorPreviewComposable = { animController, onUiStateReached ->
        AnimatedColorPreview(
            animController = animController,
            onUiStateReached = onUiStateReached,
        )
    }
    val colorPreviewDataFlow = remember {
        viewModel.viewData.mapState { requireNotNull(it).colorPreview }
    }
    val colorCenter: BareColorCenterComposable = run {
        val feature = viewModel.colorCenter
        remember(feature) {
            {
                BareColorCenter(feature = feature)
            }
        }
    }
    val selectedSwatchData: ColorDetailsViewData
    val selectedSwatchDetails: BareSelectedSwatchDetailsComposable = run {
        val feature = viewModel.selectedSwatch
        selectedSwatchData = feature.viewData.collectAsStateWithLifecycle().value
        remember(feature) {
            { modifier ->
                ColorDetailsCrossfade(
                    modifier = modifier,
                    feature = feature,
                ) { data, feature ->
                    ColorDetails(
                        data = data,
                        feature = feature,
                    )
                }
            }
        }
    }

    HomeScreen(
        data = data,
        strings = strings,
        onProceedClick = viewModel::proceed,
        onRandomizeColorClick = viewModel::randomize,
        onSelectedSwatchDismissRequest = viewModel::deselectSwatch,
        onSettingsClick = onSettingsClick,
        colorInput = colorInput,
        colorPreviewDataFlow = colorPreviewDataFlow,
        colorPreview = colorPreview,
        colorCenter = colorCenter,
        selectedSwatchData = selectedSwatchData,
        selectedSwatchDetails = selectedSwatchDetails,
        animController = animController,
        navBarAppearanceController = navBarAppearanceController,
    )
}

@Composable
private fun HomeScreen(
    data: HomeViewData,
    strings: HomeUiStrings,
    onProceedClick: () -> Unit,
    onRandomizeColorClick: () -> Unit,
    onSelectedSwatchDismissRequest: () -> Unit,
    onSettingsClick: () -> Unit,
    colorInput: BareColorInputComposable,
    colorPreviewDataFlow: StateFlow<ColorPreviewData>,
    colorPreview: BareColorPreviewComposable,
    colorCenter: BareColorCenterComposable?,
    selectedSwatchData: ColorDetailsViewData,
    selectedSwatchDetails: BareSelectedSwatchDetailsComposable?,
    animController: HomeAnimController?,
    navBarAppearanceController: NavBarAppearanceController,
) {
    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.withoutBottom(),
    ) { contentPadding ->
        Home(
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding), // ensures correct height of 'TopAppBar()'
            data = data,
            strings = strings,
            onProceedClick = onProceedClick,
            onRandomizeColorClick = onRandomizeColorClick,
            onSelectedSwatchDismissRequest = onSelectedSwatchDismissRequest,
            onSettingsClick = onSettingsClick,
            colorInput = colorInput,
            colorPreviewDataFlow = colorPreviewDataFlow,
            colorPreview = colorPreview,
            colorCenter = colorCenter,
            selectedSwatchData = selectedSwatchData,
            selectedSwatchDetails = selectedSwatchDetails,
            animController = animController,
            navBarAppearanceController = navBarAppearanceController,
        )
    }
}

@Composable
private fun Home(
    data: HomeViewData,
    strings: HomeUiStrings,
    onProceedClick: () -> Unit,
    onRandomizeColorClick: () -> Unit,
    onSelectedSwatchDismissRequest: () -> Unit,
    onSettingsClick: () -> Unit,
    colorInput: BareColorInputComposable,
    colorPreviewDataFlow: StateFlow<ColorPreviewData>,
    colorPreview: BareColorPreviewComposable,
    colorCenter: BareColorCenterComposable?,
    selectedSwatchData: ColorDetailsViewData,
    selectedSwatchDetails: BareSelectedSwatchDetailsComposable?,
    animController: HomeAnimController?,
    navBarAppearanceController: NavBarAppearanceController,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val stateOfPosInRoot = remember { mutableStateOf<Offset?>(null) }
    val focusManager = LocalFocusManager.current

    HomeLayout(
        modifier = modifier,
        scrollState = scrollState,
        onGloballyPositioned = { coords -> stateOfPosInRoot.value = coords.positionInRoot() },
        topBar = {
            TopBar(
                onSettingsClick = {
                    focusManager.clearFocus()
                    onSettingsClick()
                },
                settingsIconContentDesc = strings.settingsIconContentDesc,
            )
        },
        headline = {
            Text(
                text = strings.headline,
                style = MaterialTheme.typography.titleLarge,
            )
        },
        buttonSection = {
            ButtonSection(
                proceedButton = {
                    ProceedButton(
                        onClick = onProceedClick,
                        enabled = data.canProceed,
                        text = strings.proceedButtonText,
                    )
                },
                randomizeColorButton = {
                    RandomizeColorButton(
                        onClick = onRandomizeColorClick,
                        iconContentDesc = strings.randomizeButtonIconContentDesc,
                    )
                },
            )
        },
        colorInput = {
            ColorInputInHome(
                content = colorInput,
            )
        },
        colorPreview = {
            ColorPreviewInHome(
                flowOfData = colorPreviewDataFlow,
                homeAnimController = animController,
                containerScrollState = scrollState,
                stateOfContainerPosInRoot = stateOfPosInRoot,
                content = colorPreview,
            )
        },
        colorCenter = {
            ColorCenterInHome(
                sessionColor = data.sessionColor,
                homeAnimController = animController,
                navBarAppearanceController = navBarAppearanceController,
                containerScrollState = scrollState,
                stateOfContainerPosInRoot = stateOfPosInRoot,
                content = colorCenter,
            )
        },
    )

    SelectedSwatchDetailsInHome(
        data = selectedSwatchData,
        navBarAppearanceController = remember(navBarAppearanceController) {
            navBarAppearanceController.branch("Selected Swatch Details Dialog")
        },
        onDismissRequest = onSelectedSwatchDismissRequest,
        content = selectedSwatchDetails,
    )

    ProcessSessionColorAsSideEffect(
        sessionColor = data.sessionColor,
    )

    ScrollToTopOnNullSessionColorAsSideEffect(
        sessionColor = data.sessionColor,
        scrollState = scrollState,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    onSettingsClick: () -> Unit,
    settingsIconContentDesc: String,
) {
    val debouncedOnSettingsClick: () -> Unit = remember(onSettingsClick) {
        debounced(action = onSettingsClick)
    }
    TopAppBar(
        title = {},
        actions = {
            IconButton(onClick = debouncedOnSettingsClick) {
                Icon(
                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_settings),
                    contentDescription = settingsIconContentDesc,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        HomeScreen(
            data = previewViewData(),
            strings = previewUiStrings(),
            onProceedClick = {},
            onRandomizeColorClick = {},
            onSelectedSwatchDismissRequest = {},
            onSettingsClick = {},
            colorInput = { modifier ->
                Placeholder(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(100.dp),
                ) {
                    Text("Color Input")
                }
            },
            colorPreviewDataFlow = remember {
                val value = previewViewData().colorPreview
                MutableStateFlow(value)
            },
            colorPreview = { _, _ ->
                Placeholder(
                    Modifier.size(48.dp),
                ) {
                    Text("Color Preview")
                }
            },
            colorCenter = {
                Placeholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    color = PlaceholderDefaults.adjustedColor(LocalContentColor.current),
                ) {
                    Text("Color Center")
                }
            },
            selectedSwatchData = ColorDetailsViewData.Empty,
            selectedSwatchDetails = null,
            animController = remember {
                val currentState = HomeAnimState(
                    colorPreviewPosition = HomeAnimState.ColorPreview.Position.Dived,
                    colorPreviewVisibility = HomeAnimState.ColorPreview.Visibility.Visible,
                    colorCenter = HomeAnimState.ColorCenter.Expanded,
                )
                HomeAnimController(currentState)
            },
            navBarAppearanceController = remember { RootNavBarAppearanceController() },
        )
    }
}

private fun previewViewData() =
    HomeViewData(
        canProceed = true,
        sessionColor = HomeViewData.SessionColor(
            color = ColorInt(0x1A803F),
            isDark = true,
        ),
        colorPreview = ColorPreviewData(color = ColorInt(0x1A803F)),
    )

private fun previewUiStrings() =
    HomeUiStrings(
        settingsIconContentDesc = "Go to Settings",
        headline = "Find your color",
        proceedButtonText = "Proceed",
        randomizeButtonIconContentDesc = "Randomize color",
    )