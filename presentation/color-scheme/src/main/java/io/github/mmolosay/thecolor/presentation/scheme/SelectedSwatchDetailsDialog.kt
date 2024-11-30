package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceStack
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsOnTintedSurfaceDefaults
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.impl.TintedSurface

// This piece of UI doesn't have its own "UI" model.
// TODO: add @Preview
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedSwatchDetailsDialog(
    viewModel: ColorDetailsViewModel,
    colorSchemeUiData: ColorSchemeUiData,
    navBarAppearanceStack: NavBarAppearanceStack,
) {
    val seedData = viewModel.currentSeedDataFlow.collectAsStateWithLifecycle().value ?: return
    val surfaceColor = ColorDetailsOnTintedSurfaceDefaults.surfaceColor(seedData)
    val contentColors = ColorDetailsOnTintedSurfaceDefaults.colorsOnTintedSurface(seedData)
    val windowInsets = BottomSheetDefaults.windowInsets
    val bottomSheetWindowInsets = run {
        val sides = WindowInsetsSides.Horizontal + WindowInsetsSides.Top
        windowInsets.only(sides)
    }

    ModalBottomSheet(
        onDismissRequest = colorSchemeUiData.onSelectedSwatchDetailsDialogDismissRequest,
        containerColor = surfaceColor,
        contentColor = contentColors.accent,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = contentColors.muted,
            )
        },
        windowInsets = bottomSheetWindowInsets,
    ) {
        TintedSurface(
            surfaceColor = surfaceColor,
            contentColors = contentColors,
        ) {
            Column {
                ColorDetailsCrossfade(
                    actualDataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value,
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
//            DisposableEffect(seedData) {
//                val appearance = NavBarAppearance(
//                    color = seedData.color.toArgb(),
//                    useLightTintForControls = seedData.isDark,
//                )
//                navBarAppearanceStack.push(appearance)
//                onDispose {
//                    navBarAppearanceStack.peel()
//                }
//            }
        }
    }
}