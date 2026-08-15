package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.EnteringForeground
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.LeavingForeground
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.colorint.toCompose
import io.github.mmolosay.thecolor.presentation.common.compose.TintedSurface
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.common.toLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.design.ColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsData
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.details.viewmodel.SubjectColorData
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.utils.doNothing

/**
 * Contains "container" in name to convey that this Composable may or
 * may not display [SelectedSwatchDetailsDialog], which is its primary content.
 */
@Composable
internal fun SelectedSwatchDetailsDialogContainer(
    data: HomeData.ColorSchemeSelectedSwatchData?,
    onDismissed: () -> Unit,
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
                onDismissed()
            },
        )
    }
}

@Composable
private fun SelectedSwatchDetailsDialog(
    viewModel: ColorDetailsViewModel,
    navBarAppearanceController: NavBarAppearanceController,
    onDismissRequest: () -> Unit,
) {
    SelectedSwatchDetailsDialog(
        subjectColorData = viewModel.subjectColorDataFlow.collectAsStateWithLifecycle().value ?: return,
        colorDetailsDataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value,
        navBarAppearanceController = navBarAppearanceController,
        onDismissRequest = onDismissRequest,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedSwatchDetailsDialog(
    subjectColorData: SubjectColorData,
    colorDetailsDataState: ColorDetailsViewModel.DataState,
    navBarAppearanceController: NavBarAppearanceController,
    onDismissRequest: () -> Unit,
) {
    val surfaceColor = subjectColorData.color.toCompose()
    val colorsOnTintedSurface = if (subjectColorData.isDark) colorsOnDarkSurface() else colorsOnLightSurface()

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
    ) {
        Content(
            surfaceColor = Color.Unspecified, // already has a background due to ModalBottomSheet's 'containerColor'
            colorsOnTintedSurface = colorsOnTintedSurface,
            colorDetailsDataState = colorDetailsDataState,
        )
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    DisposableEffect(lifecycleOwner, subjectColorData) {
        val observer = ModalBottomSheetLifecycleObserver(
            navBarAppearanceController = navBarAppearanceController,
            appearance = navBarAppearance(useLightTintForControls = subjectColorData.isDark),
        ).toLifecycleEventObserver()
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
            navBarAppearanceController.clear()
        }
    }
}

@Composable
private fun Content(
    surfaceColor: Color,
    colorsOnTintedSurface: ColorsOnTintedSurface,
    colorDetailsDataState: ColorDetailsViewModel.DataState,
) {
    TintedSurface(
        surfaceColor = surfaceColor,
        contentColors = colorsOnTintedSurface,
    ) {
        ColorDetailsCrossfade(
            actualDataState = colorDetailsDataState,
        ) { state ->
            ColorDetails(
                dataState = state,
                modifier = Modifier
                    .padding(bottom = 24.dp), // just looks better this way
            )
        }
    }
}

private class ModalBottomSheetLifecycleObserver(
    private val navBarAppearanceController: NavBarAppearanceController,
    private val appearance: NavBarAppearance,
) : ExtendedLifecycleEventObserver {

    override fun onStateChanged(
        source: LifecycleOwner,
        event: Lifecycle.Event,
        directionChange: ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent?,
    ) {
        when (directionChange) {
            EnteringForeground -> {
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
                        deviation = "1366",
                    ),
                    colorRoleData = ColorDetailsData.ColorRoleData.Exact(
                        seedColor = ColorInt(0x1A803F),
                        selectSeedColor = {},
                    ),
                )
            ),
        )
    }
}