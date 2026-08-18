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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
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
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.PlaceholderDefaults
import io.github.mmolosay.thecolor.presentation.common.compose.withoutBottom
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.details.viewmodel.SubjectColorData
import io.github.mmolosay.thecolor.presentation.home.ui.center.BareColorCenterComposable
import io.github.mmolosay.thecolor.presentation.home.ui.center.ColorCenterInHome
import io.github.mmolosay.thecolor.presentation.home.ui.preview.BareColorPreviewComposable
import io.github.mmolosay.thecolor.presentation.home.ui.preview.ColorPreviewInHome
import io.github.mmolosay.thecolor.presentation.home.viewmodel.ExecuteHomeAction
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeAction
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.input.group.ColorInputGroup
import io.github.mmolosay.thecolor.presentation.preview.AnimatedColorPreview
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
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

    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value

    val flowOfUiState = remember {
        FlowOfHomeUiState(
            flowOfColorPreviewData = viewModel.colorPreviewViewModel.dataFlow,
            flowOfHomeData = viewModel.dataFlow,
        )
            // make it hot to allow replaying last value when creating 'animController'
            .shareIn(coroutineScope, SharingStarted.Eagerly, replay = 1)
    }
    val animController by produceState<HomeAnimController?>(initialValue = null) {
        val animState = flowOfUiState
            .mapNotNull { it.toAnimState() }
            .first()
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

    val colorInput: BareColorInputComposable = { modifier ->
        ColorInputGroup(
            modifier = modifier,
            viewModel = viewModel.colorInputGroupViewModel,
        )
    }
    val colorPreview: BareColorPreviewComposable = { animController, onUiStateReached ->
        AnimatedColorPreview(
            animController = animController,
            onUiStateReached = onUiStateReached,
        )
    }
    val colorPreviewDataFlow = viewModel.colorPreviewViewModel.dataFlow
    val colorCenter: BareColorCenterComposable? = run {
        val vm = viewModel.colorCenterViewModelFlow.collectAsStateWithLifecycle().value
        remember(vm) {
            if (vm == null) return@remember null
            return@remember {
                ColorCenter(
                    viewModel = vm,
                )
            }
        }
    }
    val selectedSwatchDetailsVm = viewModel.colorSchemeSwatchDetailsViewModelFlow
        .collectAsStateWithLifecycle().value
    val selectedSwatchDetails: BareSelectedSwatchDetailsComposable? = remember(selectedSwatchDetailsVm) {
        if (selectedSwatchDetailsVm == null) return@remember null
        return@remember { modifier ->
            ColorDetailsCrossfade(
                modifier = modifier,
                actualDataState = selectedSwatchDetailsVm.dataStateFlow.collectAsStateWithLifecycle().value,
            ) { state ->
                ColorDetails(
                    dataState = state,
                )
            }
        }
    }
    val selectedSwatchData = selectedSwatchDetailsVm?.subjectColorDataFlow
        ?.collectAsStateWithLifecycle()?.value

    HomeScreen(
        data = data,
        strings = strings,
        execute = viewModel::execute,
        colorInput = colorInput,
        colorPreview = colorPreview,
        colorPreviewDataFlow = colorPreviewDataFlow,
        colorCenter = colorCenter,
        selectedSwatchDetails = selectedSwatchDetails,
        selectedSwatchData = selectedSwatchData,
        animController = animController,
        navigateToSettings = navigateToSettings,
        navBarAppearanceController = navBarAppearanceController,
    )
}

@Composable
private fun HomeScreen(
    data: HomeData,
    strings: HomeUiStrings,
    execute: ExecuteHomeAction,
    colorInput: BareColorInputComposable,
    colorPreview: BareColorPreviewComposable,
    colorPreviewDataFlow: StateFlow<ColorPreviewData?>,
    colorCenter: BareColorCenterComposable?,
    selectedSwatchDetails: BareSelectedSwatchDetailsComposable?,
    selectedSwatchData: SubjectColorData?,
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
            execute = execute,
            colorInput = colorInput,
            colorPreview = colorPreview,
            colorPreviewDataFlow = colorPreviewDataFlow,
            colorCenter = colorCenter,
            selectedSwatchDetails = selectedSwatchDetails,
            selectedSwatchData = selectedSwatchData,
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
    execute: ExecuteHomeAction,
    colorInput: BareColorInputComposable,
    colorPreview: BareColorPreviewComposable,
    colorPreviewDataFlow: StateFlow<ColorPreviewData?>,
    colorCenter: BareColorCenterComposable?,
    selectedSwatchDetails: BareSelectedSwatchDetailsComposable?,
    selectedSwatchData: SubjectColorData?,
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
                onSettingsClick = { execute(HomeAction.RequestToGoToSettings) },
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
                        onClick = { execute(HomeAction.Proceed) },
                        enabled = data.canProceed,
                        text = strings.proceedButtonText,
                    )
                },
                randomizeColorButton = {
                    RandomizeColorButton(
                        onClick = { execute(HomeAction.RandomizeColor) },
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
                proceedResult = data.proceedResult,
                homeAnimController = animController,
                navBarAppearanceController = navBarAppearanceController,
                containerScrollState = scrollState,
                stateOfContainerPosInRoot = stateOfPosInRoot,
                content = colorCenter,
            )
        },
    )

    SelectedSwatchDetailsInHome(
        subjectColorData = selectedSwatchData,
        navBarAppearanceController = remember(navBarAppearanceController) {
            navBarAppearanceController.branch("Selected Swatch Details Dialog")
        },
        onDismissRequest = {
            execute(HomeAction.ClearColorSchemeSelectedSwatch)
        },
        content = selectedSwatchDetails,
    )

    ProcessSideEffectsAsSideEffect(
        sideEffects = data.sideEffects,
        onSideEffectProcessed = { se ->
            val action = HomeAction.OnSideEffectProcessed(se)
            execute(action)
        },
        navigateToSettings = navigateToSettings,
    )

    ProcessProceedResultAsSideEffect(
        proceedResult = data.proceedResult,
        strings = strings,
        clearProceedResult = { execute(HomeAction.ClearProceedResult) },
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
            execute = { Job() },
            colorInput = { modifier ->
                Placeholder(
                    modifier = modifier
                        .fillMaxWidth()
                        .height(100.dp),
                ) {
                    Text("Color Input")
                }
            },
            colorPreview = { _, _ ->
                Placeholder(
                    Modifier.size(48.dp),
                ) {
                    Text("Color Preview")
                }
            },
            colorPreviewDataFlow = remember { MutableStateFlow(null) },
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
            selectedSwatchDetails = null,
            selectedSwatchData = null,
            animController = remember {
                val currentState = HomeAnimState(
                    colorPreviewPosition = HomeAnimState.ColorPreview.Position.Dived,
                    colorPreviewVisibility = HomeAnimState.ColorPreview.Visibility.Visible,
                    colorCenter = HomeAnimState.ColorCenter.Expanded,
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
        canProceed = true,
        proceedResult = ProceedResult.Success(
            colorData = ProceedResult.Success.ColorData(
                color = ColorInt(0x1A803F),
                isDark = true,
            ),
        ),
        sideEffects = emptyList(),
    )

private fun previewUiStrings() =
    HomeUiStrings(
        settingsIconContentDesc = "Go to Settings",
        headline = "Find your color",
        proceedButtonText = "Proceed",
        randomizeButtonIconContentDesc = "Randomize color",
        invalidSubmittedColorMessage = "Please enter a valid color",
    )