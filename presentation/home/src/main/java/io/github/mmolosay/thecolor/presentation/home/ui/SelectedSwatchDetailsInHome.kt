package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.SheetValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
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
import io.github.mmolosay.thecolor.presentation.common.compose.PlaceholderDefaults
import io.github.mmolosay.thecolor.presentation.common.compose.TintedSurface
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.common.toLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewData.SessionColor
import io.github.mmolosay.thecolor.presentation.home.viewmodel.SwatchSession
import io.github.mmolosay.thecolor.utils.doNothing

/**
 * The "bare" 'Color Details' [Composable] of the selected swatch, free of any 'Home'-specific logic.
 */
internal typealias BareSelectedSwatchDetailsComposable =
        @Composable (Modifier) -> Unit

/**
 * 'Color Details' of the selected swatch as the 'Home' feature presents it.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun SelectedSwatchDetailsInHome(
    session: SwatchSession?,
    sessionColor: SessionColor?,
    navBarAppearanceController: NavBarAppearanceController,
    onDismissRequest: (SwatchSession) -> Unit,
    content: BareSelectedSwatchDetailsComposable?,
) {
    val sheetNavBarAppearanceController = remember(navBarAppearanceController) {
        navBarAppearanceController.branch("Selected Swatch Details Dialog")
    }
    if (session == null) return
    // Compose remembers internal state by where it's called, not by what it shows.
    // Without the key, a session replacing another with no frame drawn in between would reuse the old sheet:
    // still hidden if it was swiped away, and a late dismissal of the old session could close the new one
    key(session) {
        SelectedSwatchDetailsSheet(
            sessionColor = sessionColor,
            sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true),
            navBarAppearanceController = sheetNavBarAppearanceController,
            onDismissRequest = { onDismissRequest(session) },
            content = content,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun SelectedSwatchDetailsSheet(
    sessionColor: SessionColor?,
    sheetState: SheetState,
    navBarAppearanceController: NavBarAppearanceController,
    onDismissRequest: () -> Unit,
    content: BareSelectedSwatchDetailsComposable?,
) {
    if (content == null) return
    if (sessionColor == null) return

    val animatedSurfaceColor by animateColorAsState(
        targetValue = sessionColor.color.toCompose(),
        animationSpec = spring(stiffness = 100f),
        label = "surface color",
    )
    val colorsOnTintedSurface =
        if (sessionColor.isDark) colorsOnDarkSurface() else colorsOnLightSurface()

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        containerColor = animatedSurfaceColor,
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
            content(
                Modifier.padding(bottom = 24.dp), // just looks better this way
            )
        }
    }

    val lifecycleOwner = LocalLifecycleOwner.current
    val lifecycle = lifecycleOwner.lifecycle
    DisposableEffect(lifecycleOwner, sessionColor) {
        val observer = ModalBottomSheetLifecycleObserver(
            navBarAppearanceController = navBarAppearanceController,
            appearance = navBarAppearance(useLightTintForControls = sessionColor.isDark),
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

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun Preview() {
    TheColorTheme {
        // max size 'Box' is required when previewing 'ModalBottomSheet'
        Box(
            modifier = Modifier.fillMaxSize(),
        ) {
            SelectedSwatchDetailsSheet(
                sessionColor = SessionColor(
                    color = ColorInt(0x1A803F),
                    isDark = true,
                ),
                // specify initial state as Expanded to make it visible in preview
                sheetState = run {
                    val density = LocalDensity.current
                    remember(density) {
                        SheetState(
                            initialValue = SheetValue.Expanded,
                            skipPartiallyExpanded = true,
                            positionalThreshold = { with(density) { 56.dp.toPx() } },
                            velocityThreshold = { with(density) { 125.dp.toPx() } },
                        )
                    }
                },
                navBarAppearanceController = remember { RootNavBarAppearanceController() },
                onDismissRequest = {},
                content = { modifier ->
                    Placeholder(
                        modifier = modifier
                            .fillMaxWidth()
                            .height(512.dp),
                        color = PlaceholderDefaults.adjustedColor(LocalContentColor.current),
                    ) {
                        Text("Bare selected swatch details")
                    }
                },
            )
        }
    }
}