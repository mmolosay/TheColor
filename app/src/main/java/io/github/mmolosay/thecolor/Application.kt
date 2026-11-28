package io.github.mmolosay.thecolor

import android.graphics.Color
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.addFrom
import io.github.mmolosay.thecolor.presentation.common.navbar.changeNavigationBar
import io.github.mmolosay.thecolor.presentation.common.navbar.isComplete
import io.github.mmolosay.thecolor.presentation.common.navbar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.design.Brightness
import io.github.mmolosay.thecolor.presentation.design.ColorScheme
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.animateColors
import io.github.mmolosay.thecolor.presentation.design.brightness
import io.github.mmolosay.thecolor.presentation.design.toMaterialColorScheme

/**
 * A root of the entire application's UI.
 */
@Composable
internal fun Application(
    colorScheme: ColorScheme,
) {
    val navBarAppearanceController = remember { RootNavBarAppearanceController() }
    ChangeNavBarAppearanceAsSideEffect(navBarAppearanceController, colorScheme)

    val materialColorScheme = run {
        val context = LocalContext.current
        val target = remember(colorScheme) { colorScheme.toMaterialColorScheme(context) }
        val animated = target.animateColors()
        return@run animated
    }

    TheColorTheme(
        materialColorScheme = materialColorScheme,
    ) {
        MainNavDisplay(
            navBarAppearanceController = navBarAppearanceController,
        )
    }
}

@Composable
private fun ChangeNavBarAppearanceAsSideEffect(
    navBarAppearanceController: NavBarAppearanceController,
    colorScheme: ColorScheme,
) {
    val view = LocalView.current
    val defaultNavBarAppearance = navBarAppearance(
        argbColor = Color.TRANSPARENT,
        useLightTintForControls = (colorScheme.brightness() == Brightness.Dark),
    )
    LaunchedEffect(defaultNavBarAppearance) {
        navBarAppearanceController.appearanceFlow.collect { appearance ->
            val resultAppearance = if (appearance != null) {
                appearance addFrom defaultNavBarAppearance
            } else {
                defaultNavBarAppearance
            }
            check(resultAppearance.isComplete)
            view.changeNavigationBar(
                color = resultAppearance.argbColor,
                useLightTintForControls = resultAppearance.useLightTintForControls,
            )
        }
    }
}