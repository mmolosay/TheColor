package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.compose.withoutBottom
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.ui.center.BareColorCenter
import io.github.mmolosay.thecolor.presentation.home.ui.center.ColorCenterInHome
import io.github.mmolosay.thecolor.presentation.home.ui.preview.BareColorPreview
import io.github.mmolosay.thecolor.presentation.home.ui.preview.ColorPreviewInHome
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeEffect
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroup
import io.github.mmolosay.thecolor.presentation.preview.AnimatedColorPreview
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import io.github.mmolosay.thecolor.presentation.preview.toUiState
import io.github.mmolosay.thecolor.utils.ConsumableStore
import io.github.mmolosay.thecolor.utils.MutableConsumableStore
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.utils.stabilize
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.shareIn
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSettings: () -> Unit,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val context = LocalContext.current
    val strings = remember(context) { HomeUiStrings(context) }
    val coroutineScope = rememberCoroutineScope()

    val flowOfUiState = remember {
        val flowOfColorPreviewData = viewModel.colorPreviewViewModel.dataFlow
        val flowOfHomeData = viewModel.dataFlow
        fun actualUiState(): HomeUiState? {
            val isColorPreviewVisible = run {
                val data = flowOfColorPreviewData.value ?: return null
                return@run data.toUiState() is ColorPreviewUiState.Visible
            }
            val isColorCenterVisible = run {
                val data = flowOfHomeData.value
                return@run data.proceedResult is ProceedResult.Success
            }
            return HomeUiState(isColorPreviewVisible, isColorCenterVisible)
        }
        combineTransform(
            flowOfColorPreviewData,
            flowOfHomeData,
            viewModel.flowOfIsDataBeingUpdated,
        ) { _, _, isBeingUpdated ->
            // impl of 'stabilize()' that takes actual value of the flow instead of last collected
            if (!isBeingUpdated) {
                actualUiState()?.let { emit(it) }
            }
        }
            .distinctUntilChanged()
            // make it hot to allow replaying last value when creating 'animController'
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
    }
    val animController by produceState<HomeAnimController?>(initialValue = null) {
        val uiState = flowOfUiState.first()
        val animState = requireNotNull(uiState.toAnimState()) { "Invalid initial UI state" }
        value = HomeAnimController(animState)
    }
    LaunchedEffect(Unit) {
        flowOfUiState.collect { uiState ->
            val animController = animController ?: return@collect
            val to = uiState.toAnimState() ?: return@collect
            val destStates = animController.makeDestStates(to = to)
            if (destStates != null) {
                animController.run(destStates)
            }
        }
    }

    val colorInput: @Composable () -> Unit = {
        ColorInputGroup(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            viewModel = viewModel.colorInputGroupViewModel,
        )
    }
    val colorPreview: BareColorPreview = { animController, onUiStateReached ->
        AnimatedColorPreview(
            animController = animController,
            onUiStateReached = onUiStateReached,
        )
    }
    val colorCenter: BareColorCenter? = run {
        val viewModel = run {
            val upstream = viewModel.colorCenterViewModelFlow
            val flowOfColorCenterViewModel = remember {
                upstream
                    .stabilize(viewModel.flowOfIsDataBeingUpdated)
                    .distinctUntilChanged()
            }
            flowOfColorCenterViewModel
                .collectAsStateWithLifecycle(initialValue = upstream.value)
                .value
        }
        remember(viewModel) {
            if (viewModel == null) return@remember null
            return@remember {
                ColorCenter(
                    viewModel = viewModel,
                )
            }
        }
    }

    val data = run {
        val flowOfData = remember {
            viewModel.dataFlow.stabilize(viewModel.flowOfIsDataBeingUpdated)
        }
        flowOfData.collectAsStateWithLifecycle(initialValue = viewModel.dataFlow.value).value
    }

    HomeScreen(
        data = data,
        strings = strings,
        effectStore = viewModel.effectStore,
        colorInput = colorInput,
        colorPreview = colorPreview,
        colorPreviewDataFlow = viewModel.colorPreviewViewModel.dataFlow,
        colorCenter = colorCenter,
        animController = animController,
        navigateToSettings = navigateToSettings,
        navBarAppearanceController = navBarAppearanceController,
    )
}

/** Describes UI state of 'Home' View. Used to infer appropriate animation sequence / state. */
private data class HomeUiState(
    val isColorPreviewVisible: Boolean,
    val isColorCenterVisible: Boolean,
)

private fun HomeUiState.toAnimState(): HomeAnimState? =
    HomeAnimState(
        isColorPreviewVisible = this.isColorPreviewVisible,
        isColorCenterVisible = this.isColorCenterVisible,
    )

@Composable
private fun HomeScreen(
    data: HomeData,
    strings: HomeUiStrings,
    effectStore: ConsumableStore<HomeEffect>,
    colorInput: @Composable () -> Unit,
    colorPreview: BareColorPreview,
    colorPreviewDataFlow: StateFlow<ColorPreviewData?>,
    colorCenter: BareColorCenter?,
    animController: HomeAnimController?,
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
            data = data,
            strings = strings,
            effectStore = effectStore,
            colorInput = colorInput,
            colorPreview = colorPreview,
            colorPreviewDataFlow = colorPreviewDataFlow,
            colorCenter = colorCenter,
            animController = animController,
            navigateToSettings = navigateToSettings,
            navBarAppearanceController = navBarAppearanceController,
        )
    }
}

@Composable
private fun Home(
    data: HomeData,
    strings: HomeUiStrings,
    effectStore: ConsumableStore<HomeEffect>,
    colorInput: @Composable () -> Unit,
    colorPreview: BareColorPreview,
    colorPreviewDataFlow: StateFlow<ColorPreviewData?>,
    colorCenter: BareColorCenter?,
    animController: HomeAnimController?,
    navigateToSettings: () -> Unit,
    navBarAppearanceController: NavBarAppearanceController,
    modifier: Modifier = Modifier,
) {
    val scrollState = rememberScrollState()
    val stateOfPosInRoot = remember { mutableStateOf<Offset?>(null) }

    HomeLayout(
        modifier = modifier,
        scrollState = scrollState,
        onGloballyPositioned = { coords -> stateOfPosInRoot.value = coords.positionInRoot() },
        topBar = {
            TopBar(
                onSettingsClick = data.requestToGoToSettings,
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
                        onClick = (data.canProceed as? HomeData.CanProceed.Yes)?.proceed
                            ?: ::doNothing,
                        enabled = (data.canProceed is HomeData.CanProceed.Yes),
                        text = strings.proceedButtonText,
                    )
                },
                randomizeColorButton = {
                    RandomizeColorButton(
                        onClick = data.randomizeColor,
                        iconContentDesc = strings.randomizeButtonIconContentDesc,
                    )
                },
            )
        },
        colorInput = colorInput,
        colorPreview = {
            ColorPreviewInHome(
                flowOfData = colorPreviewDataFlow,
                homeAnimController = animController,
                containerScrollState = scrollState,
                stateOfContainerPosInRoot = stateOfPosInRoot,
                colorPreview = colorPreview,
            )
        },
        colorCenter = {
            ColorCenterInHome(
                proceedResult = data.proceedResult,
                homeAnimController = animController,
                navBarAppearanceController = navBarAppearanceController,
                containerScrollState = scrollState,
                stateOfContainerPosInRoot = stateOfPosInRoot,
                colorCenter = colorCenter,
            )
        },
    )

    val selectedSwatchDetailsDialogController = remember(navBarAppearanceController) {
        navBarAppearanceController.branch("Selected Swatch Details Dialog")
    }
    SelectedSwatchDetailsDialogContainer(
        data = data.colorSchemeSelectedSwatchData,
        navBarAppearanceController = selectedSwatchDetailsDialogController,
    )

    ProcessEffectsAsSideEffect(
        effectStore = effectStore,
        navigateToSettings = navigateToSettings,
    )

    ProcessProceedResultAsSideEffect(
        proceedResult = data.proceedResult,
        strings = strings,
    )

    ScrollToTopOnNullProceedResultAsSideEffect(
        proceedResult = data.proceedResult,
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
            data = previewData(),
            strings = previewUiStrings(),
            effectStore = remember { MutableConsumableStore() },
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
            colorPreview = { _, _ ->
                Text(
                    modifier = Modifier.background(Color.LightGray),
                    text = "Color Preview",
                )
            },
            colorPreviewDataFlow = remember { MutableStateFlow(null) },
            colorCenter = {
                Text(
                    modifier = Modifier
                        .background(Color.LightGray)
                        .fillMaxWidth()
                        .height(300.dp)
                        .wrapContentSize(),
                    text = "Color Center",
                )
            },
            animController = remember {
                val currentState = HomeAnimState(
                    colorPreviewPosition = HomeAnimState.ColorPreview.Position.NotDived,
                    colorPreviewVisibility = HomeAnimState.ColorPreview.Visibility.Hidden,
                    colorCenter = HomeAnimState.ColorCenter.Collapsed,
                )
                HomeAnimController(currentState)
            },
            navigateToSettings = {},
            navBarAppearanceController = remember { RootNavBarAppearanceController() },
        )
    }
}

private fun previewData() =
    HomeData(
        canProceed = HomeData.CanProceed.No,
        proceedResult = ProceedResult.Success(
            colorData = ProceedResult.Success.ColorData(
                color = ColorInt(0x1A803F),
                isDark = true,
            ),
        ),
        randomizeColor = {},
        colorSchemeSelectedSwatchData = null,
        requestToGoToSettings = {},
    )

private fun previewUiStrings() =
    HomeUiStrings(
        settingsIconContentDesc = "Go to Settings",
        headline = "Find your color",
        proceedButtonText = "Proceed",
        randomizeButtonIconContentDesc = "Randomize color",
        invalidSubmittedColorMessage = "Please enter a valid color",
    )