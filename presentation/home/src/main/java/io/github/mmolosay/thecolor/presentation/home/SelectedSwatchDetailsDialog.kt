package io.github.mmolosay.thecolor.presentation.home

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.api.ColorInt
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.nav.bar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsOnTintedSurfaceDefaults
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsSeedData
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.EnteringForeground
import io.github.mmolosay.thecolor.presentation.impl.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.LeavingForeground
import io.github.mmolosay.thecolor.presentation.impl.TintedSurface
import io.github.mmolosay.thecolor.presentation.impl.onlyBottom
import io.github.mmolosay.thecolor.presentation.impl.withoutBottom
import io.github.mmolosay.thecolor.utils.doNothing

// This piece of UI doesn't have its own "UI" model.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedSwatchDetailsDialog(
    viewModel: ColorDetailsViewModel,
    navBarAppearanceController: NavBarAppearanceController,
    onDismissRequest: () -> Unit,
) {
    SelectedSwatchDetailsDialog(
        seedData = viewModel.currentSeedDataFlow.collectAsStateWithLifecycle().value ?: return,
        colorDetailsDataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value,
        navBarAppearanceController = navBarAppearanceController,
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedSwatchDetailsDialog(
    seedData: ColorDetailsSeedData,
    colorDetailsDataState: ColorDetailsViewModel.DataState,
    navBarAppearanceController: NavBarAppearanceController,
    onDismissRequest: () -> Unit,
) {
    val surfaceColor = ColorDetailsOnTintedSurfaceDefaults.surfaceColor(seedData)
    val colorsOnTintedSurface = ColorDetailsOnTintedSurfaceDefaults.colorsOnTintedSurface(seedData)
    val windowInsets = BottomSheetDefaults.windowInsets

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
        containerColor = surfaceColor,
        contentColor = colorsOnTintedSurface.accent,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = colorsOnTintedSurface.muted,
            )
        },
        windowInsets = windowInsets.withoutBottom(),
    ) {
        Content(
            surfaceColor = Color.Unspecified, // already has a background due to ModalBottomSheet's 'containerColor'
            colorsOnTintedSurface = colorsOnTintedSurface,
            colorDetailsDataState = colorDetailsDataState,
            windowInsets = windowInsets.onlyBottom(),
        )
    }

    DisposableEffect(seedData) {
        val appearance = navBarAppearance(
            useLightTintForControls = seedData.isDark,
        )
        navBarAppearanceController.push(appearance)
        onDispose {
            navBarAppearanceController.clear()
        }
    }
//    val lifecycleOwner = LocalLifecycleOwner.current
//    val lifecycle = lifecycleOwner.lifecycle
//    DisposableEffect(lifecycleOwner, seedData) {
//        val observer = ModalBottomSheetLifecycleObserver(
//            navBarAppearanceStack = navBarAppearanceStack,
//            seedData = seedData,
//        ).toLifecycleEventObserver()
//        lifecycle.addObserver(observer)
//        onDispose {
//            lifecycle.removeObserver(observer)
//            navBarAppearanceStack.clear()
//        }
//    }
}

@Composable
private fun Content(
    surfaceColor: Color,
    colorsOnTintedSurface: ColorsOnTintedSurface,
    colorDetailsDataState: ColorDetailsViewModel.DataState,
    windowInsets: WindowInsets,
) {
    TintedSurface(
        surfaceColor = surfaceColor,
        contentColors = colorsOnTintedSurface,
    ) {
        ColorDetailsCrossfade(
            actualDataState = colorDetailsDataState,
        ) { state ->
            ColorDetails(
                state = state,
                modifier = Modifier
                    .padding(bottom = 24.dp) // just looks better this way
                    .padding(windowInsets.asPaddingValues())
                    .consumeWindowInsets(windowInsets),
            )
        }
    }
}

private class ModalBottomSheetLifecycleObserver(
    private val navBarAppearanceController: NavBarAppearanceController,
    private val seedData: ColorDetailsSeedData,
) : ExtendedLifecycleEventObserver {

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
        directionChange: ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent?,
    ) {
        when (directionChange) {
            EnteringForeground -> {
                val appearance = navBarAppearance(
                    useLightTintForControls = seedData.isDark,
                )
                navBarAppearanceController.push(appearance)
            }
            LeavingForeground -> {
                navBarAppearanceController.clear()
            }
            null -> doNothing()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
// for some reason, ModalBottomSheet doesn't show in @Preview:
// https://issuetracker.google.com/issues/283843380
private fun Preview() {
    TheColorTheme {
        Content(
            surfaceColor = Color(0xFF_1A803F),
            colorsOnTintedSurface = colorsOnDarkSurface(),
            colorDetailsDataState = ColorDetailsViewModel.DataState.Ready(
                ColorDetailsData(
                    colorName = "Jewel",
                    hex = ColorDetailsData.Hex("#1A803F"),
                    rgb = ColorDetailsData.Rgb("26", "128", "63"),
                    hsl = ColorDetailsData.Hsl("142", "66", "30"),
                    hsv = ColorDetailsData.Hsv("142", "80", "50"),
                    cmyk = ColorDetailsData.Cmyk("80", "0", "51", "50"),
                    exactMatch = ColorDetailsData.ExactMatch.No(
                        exactValue = "#126B40",
                        exactColor = ColorInt(0x126B40),
                        goToExactColor = { },
                        deviation = "1366",
                    ),
                    initialColorData = null,
                )
            ),
            windowInsets = BottomSheetDefaults.windowInsets
        )
    }
}