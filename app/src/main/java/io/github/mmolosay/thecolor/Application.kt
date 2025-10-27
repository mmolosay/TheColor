package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.rememberNavController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.addFrom
import io.github.mmolosay.thecolor.presentation.common.navbar.isComplete
import io.github.mmolosay.thecolor.presentation.common.navbar.navBarAppearance
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultNavigationBarColor
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultShouldUseLightTintForNavBarControls
import io.github.mmolosay.thecolor.presentation.common.changeNavigationBar

/**
 * A root of the entire application's UI.
 */
@Composable
internal fun Application() {
    val navController = rememberNavController()
    val rootNavBarAppearanceController = remember { RootNavBarAppearanceController() }
    val view = LocalView.current

    MainNavHost(
        navController = navController,
        rootNavBarAppearanceController = rootNavBarAppearanceController,
    )

    val defaultNavBarAppearance = navBarAppearance(
        argbColor = LocalDefaultNavigationBarColor.current,
        useLightTintForControls = LocalDefaultShouldUseLightTintForNavBarControls.current,
    )

    // change navigation bar when new appearance is emitted
    LaunchedEffect(Unit) changeNavigationBarWhenAppearanceChanges@{
        rootNavBarAppearanceController.appearanceFlow.collect { appearance ->
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