package io.github.mmolosay.thecolor.presentation.scheme

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceStack
import io.github.mmolosay.thecolor.presentation.api.push
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsOnTintedSurfaceDefaults
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel
import io.github.mmolosay.thecolor.presentation.impl.TintedSurface
import io.github.mmolosay.thecolor.presentation.impl.toArgb

// This piece of UI doesn't have its own "UI" model.
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

    ModalBottomSheet(
        onDismissRequest = colorSchemeUiData.onSelectedSwatchDetailsDialogDismissRequest,
        containerColor = surfaceColor,
        contentColor = contentColors.accent,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = contentColors.muted,
            )
        },
    ) {
        TintedSurface(
            surfaceColor = surfaceColor,
            contentColors = contentColors,
        ) {
            ColorDetailsCrossfade(
                actualDataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value,
            ) { state ->
                ColorDetails(
                    state = state,
                    modifier = Modifier.padding(bottom = 24.dp),
                )
            }
            DisposableEffect(seedData) {
                val appearance = NavBarAppearance(
                    color = seedData.color.toArgb(),
                    isLight = !seedData.isDark,
                )
                navBarAppearanceStack.push(appearance)
                onDispose {
                    navBarAppearanceStack.peel()
                }
            }
        }
    }
}