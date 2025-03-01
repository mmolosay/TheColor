package io.github.mmolosay.thecolor.presentation.home.ui

import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.center
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.DpSize
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
import io.github.mmolosay.thecolor.presentation.impl.CircularReveal
import io.github.mmolosay.thecolor.presentation.impl.CircularRevealAnimator
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent
import io.github.mmolosay.thecolor.presentation.impl.RadiusProvider
import io.github.mmolosay.thecolor.presentation.impl.TintedSurface
import io.github.mmolosay.thecolor.presentation.impl.calcVisibleHeightInScrollableParent
import io.github.mmolosay.thecolor.presentation.impl.clipCircle
import io.github.mmolosay.thecolor.presentation.impl.framesDuration
import io.github.mmolosay.thecolor.presentation.impl.onlyBottom
import io.github.mmolosay.thecolor.presentation.impl.retained
import io.github.mmolosay.thecolor.presentation.impl.retainedNotNull
import io.github.mmolosay.thecolor.presentation.impl.toCompose
import io.github.mmolosay.thecolor.presentation.impl.toDpOffset
import io.github.mmolosay.thecolor.presentation.impl.toDpSize
import io.github.mmolosay.thecolor.presentation.impl.toLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.impl.withoutBottom
import io.github.mmolosay.thecolor.presentation.input.impl.ColorInput
import io.github.mmolosay.thecolor.presentation.preview.ColorPreview
import io.github.mmolosay.thecolor.utils.cache.CacheStore
import io.github.mmolosay.thecolor.utils.cache.DequeCache
import io.github.mmolosay.thecolor.utils.cache.PruneOnSizeThreshold
import io.github.mmolosay.thecolor.utils.doNothing
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    navigateToSettings: () -> Unit,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val context = LocalContext.current
    val strings = remember(context) { HomeUiStrings(context) }
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val navEventFlow = viewModel.navEventFlow.filterNotNull()
    val selectedSwatchDetailsDialogController = remember(navBarAppearanceController) {
        navBarAppearanceController.branch("Selected Swatch Details Dialog")
    }
    val colorCenter: ColorCenterComposable? = run {
        val viewModel = viewModel.colorCenterViewModelFlow
            .collectAsStateWithLifecycle().value
            ?: return@run null
        remember(viewModel) {
            ColorCenterComposable {
                ColorCenter(
                    viewModel = viewModel,
                )
            }
        }
    }

    HomeScreen(
        data = data,
        strings = strings,
        navEventFlow = navEventFlow,
        cacheStore = viewModel.cacheStore,
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
        colorCenter = colorCenter,
        navigateToSettings = navigateToSettings,
        navBarAppearanceController = navBarAppearanceController,
    )

    SelectedSwatchDetailsDialogContainer(
        data = data.colorSchemeSelectedSwatchData,
        navBarAppearanceController = selectedSwatchDetailsDialogController,
    )
}

fun interface ColorCenterComposable {
    @Composable
    operator fun invoke()
}

@Composable
fun HomeScreen(
    data: HomeData,
    strings: HomeUiStrings,
    navEventFlow: Flow<HomeNavEvent>,
    cacheStore: CacheStore,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: ColorCenterComposable?,
    navigateToSettings: () -> Unit,
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
            cacheStore = cacheStore,
            colorInput = colorInput,
            colorPreview = colorPreview,
            colorCenter = colorCenter,
            navBarAppearanceController = navBarAppearanceController,
        )
    }

    LaunchedEffect(Unit) {
        navEventFlow.collect { event ->
            when (event) {
                is HomeNavEvent.GoToSettings -> {
                    focusManager.clearFocus()
                    navigateToSettings()
                }
            }
            event.onConsumed()
        }
    }
}

@Composable
fun Home(
    data: HomeData,
    strings: HomeUiStrings,
    cacheStore: CacheStore,
    colorInput: @Composable () -> Unit,
    colorPreview: @Composable () -> Unit,
    colorCenter: ColorCenterComposable?,
    navBarAppearanceController: NavBarAppearanceController,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()
    var positionInRoot by remember { mutableStateOf<DpOffset?>(null) }
    var size by remember { mutableStateOf<DpSize?>(null) }
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(state = scrollState)
            .onGloballyPositioned { coordinates ->
                positionInRoot = coordinates
                    .positionInRoot()
                    .toDpOffset(density)
                size = coordinates.size.toDpSize(density)
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
        // TODO: animated color preview is a part of bigger animation, see AnimatedColorCenter.kt
//        AnimatedColorPreview(
//            colorPreview = colorPreview,
//            state = uiData.colorPreviewState,
//            containerSize = size,
//            containerPositionInRoot = positionInRoot,
//        )
        colorPreview()

        Spacer(modifier = Modifier.height(16.dp))
//        AnimatedColorCenter {
        ColorCenterContainer(
            colorCenter = colorCenter,
            proceedResult = data.proceedResult,
            cacheStore = cacheStore,
            navBarAppearanceController = navBarAppearanceController,
            parentScrollState = scrollState,
        )
//        }
    }

    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    val proceedResult = data.proceedResult
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
        // animationSpec is default to be the same as in Color Preview
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

/**
 * Contains "container" in name to convey that this Composable may or
 * may not display [colorCenter], which is its primary content.
 */
@Composable
private fun ColorCenterContainer(
    colorCenter: ColorCenterComposable?,
    proceedResult: ProceedResult?,
    cacheStore: CacheStore,
    navBarAppearanceController: NavBarAppearanceController,
    parentScrollState: ScrollState,
) {
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    val proceedResultCacheTag = CacheStore.Tag("ProceedResultCacheTag")
    val proceedResultCache = cacheStore.getOrNew<ProceedResult?>(proceedResultCacheTag) {
        DequeCache(
            mutationListener = PruneOnSizeThreshold(
                cacheSizeThreshold = 10, numberOfLatestElementsToKeep = 2,
            ),
        )
    }
    fun currentAndPreviousFromCache(): Pair<ProceedResult?, ProceedResult?> {
        val values = proceedResultCache.asReversed()
        val current = values.getOrNull(0)
        val previous = values.getOrNull(1)
        return Pair(current, previous)
    }
    fun hasProceedResultBecomeSuccess(): Boolean {
        val (current, previous) = currentAndPreviousFromCache()
        return (current is ProceedResult.Success && previous !is ProceedResult.Success)
    }
    fun hasProceedResultBecomeNull(): Boolean {
        val (current, previous) = currentAndPreviousFromCache()
        return (current == null && previous is ProceedResult.Success)
    }

    val retainedProceedResult = retained(proceedResult) { actual, memoized ->
        val memoizedIsSuccess = (memoized is ProceedResult.Success)
        val actualIsNotSuccess = (actual !is ProceedResult.Success)
        if (memoizedIsSuccess && actualIsNotSuccess) {
            delay(2.framesDuration)
        }
        value = actual
    }
    LaunchedEffect(retainedProceedResult) {
        proceedResultCache += retainedProceedResult
    }

    val circularRevealAnimator = remember {
        val initialProgressValue = when {
            retainedProceedResult is ProceedResult.Success -> CircularRevealAnimator.FullyExpandedValue
            else -> CircularRevealAnimator.FullyCollapsedValue
        }
        CircularRevealAnimator(
            progressAnimatable = Animatable(initialValue = initialProgressValue),
            expandAnimationSpec = spring(stiffness = 100f),
            collapseAnimationSpec = spring(stiffness = 300f),
        )
    }
    LaunchedEffect(retainedProceedResult) {
        when {
            hasProceedResultBecomeSuccess() -> {
                coroutineScope.launch { circularRevealAnimator.expand() }
            }
            hasProceedResultBecomeNull() -> {
                coroutineScope.launch { circularRevealAnimator.collapse() }
            }
        }
    }

    /** Wrapper for decorated [colorCenter] with specific positioning inside the parent and animations. */
    @Composable
    fun ColorCenter(
        data: ProceedResult.Success,
        colorCenter: ColorCenterComposable,
    ) {
        // calculate min height of Color Center so that its bottom matches bottom of the parent Column
        var minHeight by remember { mutableStateOf<Dp>(Dp.Unspecified) }
        var visibleHeightInParent by remember { mutableStateOf<Float?>(null) }

        ColorCenter(
            modifier = Modifier
                .onPlaced { coordinates ->
                    val ownYPos = coordinates.positionInParent().y
                    val parentHeight = parentScrollState.viewportSize
                    val ownMinHeight = parentHeight - ownYPos
                    minHeight = with(density) { ownMinHeight.toDp() }
                }
                .onGloballyPositioned { coordinates ->
                    val ownPosInParent = coordinates.positionInParent()
                    visibleHeightInParent = calcVisibleHeightInScrollableParent(
                        parentScrollState = parentScrollState,
                        ownPosInParent = ownPosInParent.y,
                    )
                }
                .clipCircle(
                    center = { size ->
                        val h = visibleHeightInParent
                        if (h != null && h != 0f) Offset(x = size.width / 2, y = h)
                        else size.center
                    },
                    radius = RadiusProvider { size, minCoverRadius ->
                        minCoverRadius * circularRevealAnimator.progressAnimatable.value
                    },
                ),
            surfaceColor = data.colorData.color.toCompose(),
            isSurfaceColorDark = data.colorData.isDark,
            colorCenter = colorCenter,
            navBarAppearanceController = navBarAppearanceController,
            minHeight = minHeight,
        )
    }

    val actualColorCenter: ColorCenterComposable? =
        remember(colorCenter, retainedProceedResult) {
            colorCenter ?: return@remember null
            val retainedAsSuccess = (retainedProceedResult as? ProceedResult.Success) ?: return@remember null
            ColorCenterComposable {
                ColorCenter(
                    data = retainedAsSuccess,
                    colorCenter = colorCenter,
                )
            }
        }
    val retainedColorCenter = retainedNotNull(actualValue = actualColorCenter)

    CircularReveal(
        animator = circularRevealAnimator,
    ) {
        retainedColorCenter?.invoke()
    }
}

/** Decorates bare [colorCenter] in a way that's specific for this screen. */
@Composable
private fun ColorCenter(
    surfaceColor: Color,
    isSurfaceColorDark: Boolean,
    colorCenter: ColorCenterComposable,
    navBarAppearanceController: NavBarAppearanceController,
    modifier: Modifier = Modifier,
    minHeight: Dp,
) {
    val animationSpec = spring<Color>(stiffness = 100f)
    val contentColors = if (isSurfaceColorDark) colorsOnDarkSurface() else colorsOnLightSurface()
    val animatedContentColors = contentColors.animate(
        animationSpec = animationSpec,
    )
    val animatedSurfaceColor by animateColorAsState(
        targetValue = surfaceColor,
        animationSpec = animationSpec,
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
                .sizeIn(minHeight = minHeight) // it's important to set size before paddings
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
            navEventFlow = emptyFlow(),
            cacheStore = remember { CacheStore() },
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
                    text = "Color Center",
                )
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