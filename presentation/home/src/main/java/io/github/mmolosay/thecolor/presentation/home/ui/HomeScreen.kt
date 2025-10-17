package io.github.mmolosay.thecolor.presentation.home.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Casino
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.nav.bar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.nav.bar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.center.ColorCenter
import io.github.mmolosay.thecolor.presentation.center.ColorCenterShape
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.animate
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeNavEvent
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent
import io.github.mmolosay.thecolor.presentation.impl.TintedSurface
import io.github.mmolosay.thecolor.presentation.impl.onlyBottom
import io.github.mmolosay.thecolor.presentation.impl.toCompose
import io.github.mmolosay.thecolor.presentation.impl.toLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.impl.withoutBottom
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInput
import io.github.mmolosay.thecolor.presentation.preview.AnimatedColorPreview
import io.github.mmolosay.thecolor.utils.cache.DequeCache
import io.github.mmolosay.thecolor.utils.cache.PruneOnSizeThreshold
import io.github.mmolosay.thecolor.utils.doNothing
import io.github.mmolosay.thecolor.utils.stabilize
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.combineTransform
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.shareIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSettings: () -> Unit,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val strings = remember(context) { HomeUiStrings(context) }
    val selectedSwatchDetailsDialogController = remember(navBarAppearanceController) {
        navBarAppearanceController.branch("Selected Swatch Details Dialog")
    }
    val colorInput: @Composable () -> Unit = {
        ColorInput(
            viewModel = viewModel.colorInputViewModel,
        )
    }
    val colorPreview: ColorPreviewWithDependencies = remember {
        ColorPreviewWithDependencies(
            viewModel = viewModel.colorPreviewViewModel,
        ) { animController, onUiStateReached ->
            AnimatedColorPreview(
                animController = animController,
                onUiStateReached = onUiStateReached,
            )
        }
    }
    val colorCenter: ColorCenterComposable? = run {
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

    val flowOfUiState = remember {
        val florOfColorPreviewData = viewModel.colorPreviewViewModel.dataFlow
        val flowOfHomeData = viewModel.dataFlow
        fun actualUiState(): HomeUiState? {
            val isColorPreviewVisible = run {
                val data = florOfColorPreviewData.value ?: return null
                isColorPreviewVisible(data)
            }
            val isColorCenterVisible = run {
                val data = flowOfHomeData.value
                isColorCenterVisible(data.proceedResult)
            }
            return HomeUiState(isColorPreviewVisible, isColorCenterVisible)
        }
        val signal = Any()
        combine(
            florOfColorPreviewData,
            flowOfHomeData,
            transform = { _, _ -> signal }, // discard values and just emit "something has changed" signal
        )
            // impl of 'stabilize()' that takes actual value of the flow instead of last collected
            .combineTransform(viewModel.flowOfIsDataBeingUpdated) { signal, isBeingUpdated ->
                if (!isBeingUpdated) {
                    actualUiState()?.let { emit(it) }
                }
            }
            .distinctUntilChanged()
            // make it hot to allow replaying last value when creating 'animController'
            .shareIn(coroutineScope, SharingStarted.WhileSubscribed(), replay = 1)
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

    val data = run {
        val flowOfData = remember {
            viewModel.dataFlow.stabilize(viewModel.flowOfIsDataBeingUpdated)
        }
        flowOfData.collectAsStateWithLifecycle(initialValue = viewModel.dataFlow.value).value
    }

    HomeScreen(
        data = data,
        strings = strings,
        navEventFlow = viewModel.navEventFlow,
        colorInput = colorInput,
        colorPreview = colorPreview,
        colorCenter = colorCenter,
        navigateToSettings = navigateToSettings,
        animController = animController,
        navBarAppearanceController = navBarAppearanceController,
    )

    SelectedSwatchDetailsDialogContainer(
        data = data.colorSchemeSelectedSwatchData,
        navBarAppearanceController = selectedSwatchDetailsDialogController,
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

// syntactic sugar that makes nullable types easier to read
private typealias ColorCenterComposable = @Composable () -> Unit

@Composable
private fun HomeScreen(
    data: HomeData,
    strings: HomeUiStrings,
    navEventFlow: Flow<HomeNavEvent>,
    colorInput: @Composable () -> Unit,
    colorPreview: ColorPreviewWithDependencies,
    colorCenter: ColorCenterComposable?,
    navigateToSettings: () -> Unit,
    animController: HomeAnimController?,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val focusManager = LocalFocusManager.current

    Scaffold(
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.withoutBottom(),
    ) { contentPadding ->
        Home(
            modifier = Modifier
                .padding(contentPadding)
                .consumeWindowInsets(contentPadding), // ensures correct height of 'TopAppBar()'
            data = data,
            strings = strings,
            colorInput = colorInput,
            colorPreview = colorPreview,
            colorCenter = colorCenter,
            animController = animController,
            navBarAppearanceController = navBarAppearanceController,
        )
    }

    LaunchedEffect(Unit) {
        launch(Dispatchers.Main.immediate) {
            navEventFlow.collect { event ->
                when (event) {
                    is HomeNavEvent.GoToSettings -> {
                        focusManager.clearFocus()
                        navigateToSettings()
                    }
                }
            }
        }
    }
}

@Composable
private fun Home(
    data: HomeData,
    strings: HomeUiStrings,
    colorInput: @Composable () -> Unit,
    colorPreview: ColorPreviewWithDependencies,
    colorCenter: ColorCenterComposable?,
    animController: HomeAnimController?,
    navBarAppearanceController: NavBarAppearanceController,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val proceedResult = data.proceedResult

    val scrollState = rememberScrollState()
    val stateOfViewportHeight = produceState<Int?>(initialValue = null, /*keys*/ scrollState.viewportSize) {
        value = scrollState.viewportSize.takeUnless { it == 0 } // consider 0 size as unknown
    }
    val stateOfPosInRoot = remember { mutableStateOf<Offset?>(null) }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .onGloballyPositioned { coordinates ->
                stateOfPosInRoot.value = coordinates.positionInRoot()
            },
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TopBar(
            onSettingsClick = data.requestToGoToSettings,
            settingsIconContentDesc = strings.settingsIconContentDesc,
        )

        Spacer(modifier = Modifier.height(160.dp))
        Text(
            text = strings.headline,
            style = MaterialTheme.typography.titleLarge,
        )

        Spacer(modifier = Modifier.height(16.dp))
        colorInput()

        Spacer(modifier = Modifier.height(8.dp))
        ButtonSection(
            proceedButton = {
                ProceedButton(
                    onClick = (data.canProceed as? HomeData.CanProceed.Yes)?.proceed ?: ::doNothing,
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

        Spacer(modifier = Modifier.height(8.dp))

        if (animController != null) {
            AnimatedColorPreview(
                colorPreview = colorPreview,
                flowOfPositionAnimDest = kotlin.run {
                    val upstream = animController.flowOfDestState
                    remember(upstream) {
                        fun value(animState: HomeAnimState) = animState.colorPreviewPosition
                        upstream
                            .map(::value)
                            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), value(upstream.value))
                    }
                },
                flowOfVisibilityAnimDest = kotlin.run {
                    val upstream = animController.flowOfDestState
                    remember(upstream) {
                        fun value(animState: HomeAnimState) = animState.colorPreviewVisibility
                        upstream
                            .map(::value)
                            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), value(upstream.value))
                    }
                },
                onPositionReached = animController::onValueReached,
                onVisibilityReached = animController::onValueReached,
                stateOfContainerViewportHeight = stateOfViewportHeight,
                stateOfContainerPosInRoot = stateOfPosInRoot,
            )
            val decoratedColorCenter = remember(colorCenter, proceedResult) {
                decoratedColorCenterComposable(
                    colorCenter = colorCenter,
                    proceededColorData = (proceedResult as? ProceedResult.Success)?.colorData,
                    navBarAppearanceController = navBarAppearanceController,
                    containerScrollState = scrollState,
                    stateOfContainerPosInRoot = stateOfPosInRoot,
                )
            }
            AnimatedColorCenter(
                colorCenter = decoratedColorCenter,
                flowOfAnimDest = kotlin.run {
                    val upstream = animController.flowOfDestState
                    remember(upstream) {
                        fun value(animState: HomeAnimState) = animState.colorCenter
                        upstream
                            .map(::value)
                            .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), value(upstream.value))
                    }
                },
                onReached = animController::onValueReached,
                containerScrollState = scrollState,
            )
        }
    }

    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(proceedResult) {
        when (proceedResult) {
            is ProceedResult.Success -> {
                // keyboard blinks when hidden, similar issue: https://stackoverflow.com/q/76901241/8862499
                // the issue is somewhere in 'Color Input', probably in the internals of TextField()
                softwareKeyboardController?.hide()
            }
            is ProceedResult.InvalidSubmittedColor -> {
                Toast
                    .makeText(context, strings.invalidSubmittedColorMessage, Toast.LENGTH_SHORT)
                    .show()
                proceedResult.discard()
            }
            null -> doNothing()
        }
    }

    ScrollToTopOnNullProceedResultAsSideEffect(
        proceedResult = proceedResult,
        scrollState = scrollState,
    )
}

@Composable
private fun ButtonSection(
    proceedButton: @Composable () -> Unit,
    randomizeColorButton: @Composable () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = ButtonSectionHorizontalArrangement,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        proceedButton()
        randomizeColorButton()
    }
}

@Composable
private fun ProceedButton(
    onClick: () -> Unit,
    enabled: Boolean,
    text: String,
) {
    val keyboardController = LocalSoftwareKeyboardController.current
    val wrappedOnClick: () -> Unit = {
        onClick()
        keyboardController?.hide()
    }
    val colors = ButtonDefaults.buttonColors()
    val colorsAnimationProgress by animateFloatAsState(
        targetValue = if (enabled) 1f else 0f,
        // animationSpec is kept default to be the same as in Color Preview
        label = "proceed button colors",
    )
    val animatedColors = ButtonColors(
        containerColor = lerp(colors.disabledContainerColor, colors.containerColor, colorsAnimationProgress),
        contentColor = lerp(colors.disabledContentColor, colors.contentColor, colorsAnimationProgress),
        disabledContainerColor = lerp(colors.disabledContainerColor, colors.containerColor, colorsAnimationProgress),
        disabledContentColor = lerp(colors.disabledContentColor, colors.contentColor, colorsAnimationProgress),
    )
    Button(
        onClick = wrappedOnClick,
        enabled = enabled,
        colors = animatedColors
    ) {
        Text(text = text)
    }
}

@Composable
private fun RandomizeColorButton(
    onClick: () -> Unit,
    iconContentDesc: String,
) {
    var rotationDest by remember { mutableFloatStateOf(0f) } // degrees
    val animatedRotation by animateFloatAsState(
        targetValue = rotationDest,
        animationSpec = spring(
            stiffness = Spring.StiffnessMediumLow,
            dampingRatio = Spring.DampingRatioMediumBouncy,
        ),
        label = "randomize color button icon rotation",
    )
    fun rotate() {
        val clockwise = Random.nextBoolean()
        val rotationMult = if (clockwise) +1 else -1 // see 'rotate()' Modifier
        rotationDest += (90 * rotationMult)
    }
    val wrappedOnClick: () -> Unit = {
        onClick()
        rotate()
    }
    FilledTonalIconButton(
        onClick = wrappedOnClick,
    ) {
        Icon(
            modifier = Modifier
                .size(20.dp)
                .rotate(animatedRotation),
            imageVector = Icons.Outlined.Casino,
            contentDescription = iconContentDesc,
        )
    }
}

private fun decoratedColorCenterComposable(
    colorCenter: ColorCenterComposable?,
    proceededColorData: ProceedResult.Success.ColorData?,
    navBarAppearanceController: NavBarAppearanceController,
    containerScrollState: ScrollState,
    stateOfContainerPosInRoot: State<Offset?>,
): ColorCenterComposable? {
    if (colorCenter == null) return null
    if (proceededColorData == null) return null
    return {
        val density = LocalDensity.current
        val stateOfMinHeight = remember { mutableStateOf<Dp>(Dp.Unspecified) }
        DecoratedColorCenter(
            modifier = Modifier
                .onPlaced { coordinates ->
                    // calculate min height of Color Center so that its bottom matches bottom of the parent Column
                    val containerPosInRoot = stateOfContainerPosInRoot.value ?: return@onPlaced
                    val containerHeight = containerScrollState.viewportSize
                    val ownPosInRoot = coordinates.positionInRoot()
                    val ownYPosInContainer = (ownPosInRoot - containerPosInRoot).y
                    stateOfMinHeight.value =
                        with(density) { (containerHeight - ownYPosInContainer).toDp() }
                },
            surfaceColor = proceededColorData.color.toCompose(),
            isSurfaceColorDark = proceededColorData.isDark,
            colorCenter = colorCenter,
            navBarAppearanceController = navBarAppearanceController,
            stateOfMinHeight = stateOfMinHeight,
        )
    }
}

/** Decorates bare [colorCenter] in a way that's specific for this screen. */
@Composable
private fun DecoratedColorCenter(
    surfaceColor: Color,
    isSurfaceColorDark: Boolean,
    colorCenter: ColorCenterComposable,
    navBarAppearanceController: NavBarAppearanceController,
    modifier: Modifier = Modifier,
    stateOfMinHeight: State<Dp>, // wrapped in State to avoid recompositions
) {
    fun <T> animationSpec() = spring<T>(stiffness = 100f)
    val contentColors = if (isSurfaceColorDark) colorsOnDarkSurface() else colorsOnLightSurface()
    val animatedContentColors = contentColors.animate(animationSpec())
    val animatedSurfaceColor by animateColorAsState(
        targetValue = surfaceColor,
        animationSpec = animationSpec(),
        label = "surface color",
    )
    TintedSurface(
        modifier = modifier
            .graphicsLayer {
                clip = true
                shape = ColorCenterShape
            },
        surfaceColor = animatedSurfaceColor,
        contentColors = animatedContentColors,
    ) {
        val windowInsets = WindowInsets.systemBars.onlyBottom()
        Box(
            modifier = Modifier
                .sizeIn(minHeight = stateOfMinHeight.value) // it's important to set size before paddings
                .padding(windowInsets.asPaddingValues())
                .consumeWindowInsets(windowInsets)
                .padding(top = 24.dp), // to accommodate to convex 'ColorCenterShape'
            propagateMinConstraints = true,
        ) {
            colorCenter()
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    DisposableEffect(lifecycleOwner, surfaceColor, isSurfaceColorDark) {
        val observer = ColorCenterLifecycleObserver(
            navBarAppearanceController = navBarAppearanceController,
            appearance = navBarAppearance(useLightTintForControls = isSurfaceColorDark),
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
                    imageVector = Icons.Rounded.Settings,
                    contentDescription = settingsIconContentDesc,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        },
    )
}

/**
 * Contains "container" in name to convey that this Composable may or
 * may not display [SelectedSwatchDetailsDialog], which is its primary content.
 */
@Composable
private fun SelectedSwatchDetailsDialogContainer(
    data: HomeData.ColorSchemeSelectedSwatchData?,
    navBarAppearanceController: NavBarAppearanceController,
) {
    var showSelectedSwatchDetailsDialog by remember { mutableStateOf(false) }

    LaunchedEffect(data) {
        if (data != null) {
            showSelectedSwatchDetailsDialog = true
        }
    }
    if (showSelectedSwatchDetailsDialog) {
        @Suppress("NAME_SHADOWING")
        val data = requireNotNull(data)
        SelectedSwatchDetailsDialog(
            viewModel = data.colorDetailsViewModel,
            navBarAppearanceController = navBarAppearanceController,
            onDismissRequest = {
                showSelectedSwatchDetailsDialog = false
                data.discard()
            },
        )
    }
}

@Composable
private fun ScrollToTopOnNullProceedResultAsSideEffect(
    proceedResult: ProceedResult?,
    scrollState: ScrollState,
) {
    val cacheOfProceedResult = remember {
        DequeCache<ProceedResult?>(
            mutationListener = PruneOnSizeThreshold(cacheSizeThreshold = 2),
        )
    }
    LaunchedEffect(proceedResult) {
        val current = proceedResult
        // previous may be present but equal to 'null'
        if (cacheOfProceedResult.isNotEmpty()) {
            val previous = cacheOfProceedResult.last()
            val wasSuccessButBecameNull = (previous is ProceedResult.Success && current == null)
            if (wasSuccessButBecameNull && scrollState.value != 0) {
                scrollState.animateScrollTo(0)
            }
        }
        cacheOfProceedResult += proceedResult
    }
}

/**
 * An [Arrangement] for [ButtonSection].
 * Places first element right in the center of the container.
 * Places rest elements after the first one.
 */
@Immutable
private object ButtonSectionHorizontalArrangement : Arrangement.Horizontal {

    override val spacing = 8.dp

    override fun Density.arrange(
        totalSize: Int,
        sizes: IntArray,
        layoutDirection: LayoutDirection,
        outPositions: IntArray,
    ) {
        val firstChildSize = sizes.firstOrNull() ?: return
        val firstChildPos = (totalSize / 2) - (firstChildSize / 2)
        outPositions[0] = firstChildPos
        if (sizes.size == 1) return
        val sizesWithIndices = sizes.mapIndexed { index, size ->
            index to size
        }
        val sizesWithIndicesWithoutFirstChild = sizesWithIndices.drop(1)
        val spacingPx = spacing.roundToPx()
        var endOfLastPlacedChild = when (layoutDirection) {
            LayoutDirection.Ltr -> firstChildPos + firstChildSize
            LayoutDirection.Rtl -> firstChildPos
        }
        sizesWithIndicesWithoutFirstChild.forEach { (index, size) ->
            val pos = when (layoutDirection) {
                LayoutDirection.Ltr -> endOfLastPlacedChild + spacingPx
                LayoutDirection.Rtl -> endOfLastPlacedChild - spacingPx - size
            }
            outPositions[index] = pos
            endOfLastPlacedChild = when (layoutDirection) {
                LayoutDirection.Ltr -> pos + size
                LayoutDirection.Rtl -> pos
            }
        }
    }
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
            data = previewData(),
            strings = previewUiStrings(),
            navEventFlow = remember { emptyFlow() },
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
            colorPreview = remember {
                NoopColorPreviewWithDependencies { _, _ ->
                    Text(
                        modifier = Modifier.background(Color.LightGray),
                        text = "Color Preview",
                    )
                }
            },
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
            navigateToSettings = {},
            animController = remember {
                val currentState = HomeAnimState(
                    colorPreviewPosition = HomeAnimState.ColorPreview.Position.NotDived,
                    colorPreviewVisibility = HomeAnimState.ColorPreview.Visibility.Hidden,
                    colorCenter = HomeAnimState.ColorCenter.Collapsed,
                )
                HomeAnimController(currentState)
            },
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