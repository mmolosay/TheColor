package io.github.mmolosay.thecolor.presentation.home.ui

import android.content.res.Configuration
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import io.github.mmolosay.thecolor.presentation.center.ColorCenterShape
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.common.ExtendedLifecycleEventObserver.LifecycleDirectionChangeEvent
import io.github.mmolosay.thecolor.presentation.common.colorint.toCompose
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.TintedSurface
import io.github.mmolosay.thecolor.presentation.common.compose.onlyBottom
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.common.toLifecycleEventObserver
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.animate
import io.github.mmolosay.thecolor.presentation.design.colorsOnDarkSurface
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData.ProceedResult
import io.github.mmolosay.thecolor.utils.doNothing

internal fun decoratedColorCenterComposable(
    colorCenter: (@Composable () -> Unit)?,
    proceededColorData: ProceedResult.Success.ColorData?,
    navBarAppearanceController: NavBarAppearanceController,
    containerScrollState: ScrollState,
    stateOfContainerPosInRoot: State<Offset?>,
): (@Composable () -> Unit)? {
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
                    stateOfMinHeight.value = with(density) { (containerHeight - ownYPosInContainer).toDp() }
                },
            surfaceColor = proceededColorData.color.toCompose(),
            isSurfaceColorDark = proceededColorData.isDark,
            navBarAppearanceController = navBarAppearanceController,
            stateOfMinHeight = stateOfMinHeight,
            content = colorCenter,
        )
    }
}

/**
 * Decorates bare 'Color Center' [content] in a way that's specific for the 'Home' screen.
 */
@Composable
internal fun DecoratedColorCenter(
    modifier: Modifier = Modifier,
    surfaceColor: Color,
    isSurfaceColorDark: Boolean,
    navBarAppearanceController: NavBarAppearanceController,
    stateOfMinHeight: State<Dp>, // wrapped in State to avoid recompositions
    content: @Composable () -> Unit,
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
            content()
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

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            DecoratedColorCenter(
                surfaceColor = Color(0xFF_1A803F),
                isSurfaceColorDark = false,
                navBarAppearanceController = remember { RootNavBarAppearanceController() },
                stateOfMinHeight = remember { mutableStateOf(196.dp) },
            ) {
                Placeholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(196.dp),
                ) {
                    Text("Color Center")
                }
            }
        }
    }
}