package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.EnteringForeground
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent.LeavingForeground
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.colorint.toCompose
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.TintedSurface
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.common.toLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.details.viewmodel.SubjectColorData
import io.github.mmolosay.thecolor.utils.doNothing

/**
 * The "bare" 'Color Details' [Composable] of the selected swatch, free of any 'Home'-specific logic.
 */
internal typealias BareSelectedSwatchDetails =
        @Composable (Modifier) -> Unit

/**
 * 'Color Details' of the selected swatch as the 'Home' feature presents it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedSwatchDetailsInHome(
    selectedSwatchDetails: BareSelectedSwatchDetails?,
    subjectColorData: SubjectColorData?,
    navBarAppearanceController: NavBarAppearanceController,
    onDismissRequest: () -> Unit,
) {
    if (selectedSwatchDetails == null) return
    if (subjectColorData == null) return

    val surfaceColor = subjectColorData.color.toCompose()
    val colorsOnTintedSurface =
        if (subjectColorData.isDark) colorsOnDarkSurface() else colorsOnLightSurface()

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
        TintedSurface(
            surfaceColor = Color.Unspecified, // already has a background due to ModalBottomSheet's 'containerColor'
            contentColors = colorsOnTintedSurface,
        ) {
            selectedSwatchDetails(
                Modifier.padding(bottom = 24.dp), // just looks better this way
            )
        }
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
            EnteringForeground -> navBarAppearanceController.push(appearance)
            LeavingForeground -> navBarAppearanceController.clear()
            null -> doNothing()
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview
@Composable
private fun Preview() {
    TheColorTheme {
        // max size 'Box' is required when previewing 'ModalBottomSheet'
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            SelectedSwatchDetailsInHome(
                selectedSwatchDetails = { modifier ->
                    Placeholder(
                        modifier = modifier
                            .fillMaxWidth()
                            .height(512.dp),
                        color = MaterialTheme.colorScheme.onSurface,
                    ) {
                        Text("Bare selected swatch details")
                    }
                },
                subjectColorData = SubjectColorData(
                    color = ColorInt(0x1A803F),
                    isDark = true,
                ),
                navBarAppearanceController = remember { RootNavBarAppearanceController() },
                onDismissRequest = {},
            )
        }
    }
}