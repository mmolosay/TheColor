package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
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
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceStack
import io.github.mmolosay.thecolor.presentation.api.nav.bar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.api.nav.bar.push
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
import io.github.mmolosay.thecolor.utils.doNothing
import java.util.Optional

// This piece of UI doesn't have its own "UI" model.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedSwatchDetailsDialog(
    viewModel: ColorDetailsViewModel,
    colorSchemeUiData: ColorSchemeUiData,
    navBarAppearanceController: NavBarAppearanceController,
) {
    SelectedSwatchDetailsDialog(
        onDismissRequest = colorSchemeUiData.onSelectedSwatchDetailsDialogDismissRequest,
        seedData = viewModel.currentSeedDataFlow.collectAsStateWithLifecycle().value ?: return,
        colorDetailsDataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value,
        navBarAppearanceController = navBarAppearanceController,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedSwatchDetailsDialog(
    onDismissRequest: () -> Unit,
    seedData: ColorDetailsSeedData,
    colorDetailsDataState: ColorDetailsViewModel.DataState,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val surfaceColor = ColorDetailsOnTintedSurfaceDefaults.surfaceColor(seedData)
    val colorsOnTintedSurface = ColorDetailsOnTintedSurfaceDefaults.colorsOnTintedSurface(seedData)
    val windowInsets = BottomSheetDefaults.windowInsets
    val bottomSheetWindowInsets = run {
        val sides = WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        windowInsets.only(sides)
    }

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
        windowInsets = bottomSheetWindowInsets,
    ) {
        Content(
            surfaceColor = Color.Unspecified, // already has a background due to ModalBottomSheet's 'containerColor'
            colorsOnTintedSurface = colorsOnTintedSurface,
            colorDetailsDataState = colorDetailsDataState,
            windowInsets = windowInsets,
        )
    }

    DisposableEffect(seedData) {
        val appearance = navBarAppearance(
            useLightTintForControls = seedData.isDark.let { Optional.of(it) },
        )
        navBarAppearanceController.stack.push(appearance)
        onDispose {
            navBarAppearanceController.stack.clear()
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
        Column {
            ColorDetailsCrossfade(
                actualDataState = colorDetailsDataState,
            ) { state ->
                ColorDetails(
                    state = state,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }

            val bottomWindowInsets = windowInsets.only(WindowInsetsSides.Bottom)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(bottomWindowInsets)
                    .consumeWindowInsets(bottomWindowInsets)
            )
        }
    }
}

private class ModalBottomSheetLifecycleObserver(
    private val navBarAppearanceStack: NavBarAppearanceStack,
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
                    useLightTintForControls = seedData.isDark.let { Optional.of(it) },
                )
                navBarAppearanceStack.push(appearance)
            }
            LeavingForeground -> {
                navBarAppearanceStack.clear()
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