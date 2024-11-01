package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultNavigationBarColor
import io.github.mmolosay.thecolor.presentation.design.LocalIsDefaultNavigationBarLight
import io.github.mmolosay.thecolor.presentation.impl.changeNavigationBar
import io.github.mmolosay.thecolor.presentation.impl.toArgb

/**
 * A root of the entire application's UI.
 */
@Composable
internal fun Application() {
    val navController = rememberNavController()
    val navBarAppearanceController = remember { NavBarAppearanceController() }
    val view = LocalView.current

    MainNavHost(
        navController = navController,
        navBarAppearanceController = navBarAppearanceController,
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val defaultNavBarColor = LocalDefaultNavigationBarColor.current
    val isDefaultNavBarLight = LocalIsDefaultNavigationBarLight.current

    // change navigation bar when screen changes
    LaunchedEffect(backStackEntry) {
        backStackEntry ?: return@LaunchedEffect
        // Immediately restore nav bar when screen changes.
        // If some screen wants to style nav bar in its own way, it may do so.
        view.changeNavigationBar(
            color = defaultNavBarColor,
            isLight = isDefaultNavBarLight,
        )
    }

    // change navigation bar when new appearance is emitted
    LaunchedEffect(Unit) changeNavigationBarWhenAppearanceChanges@{
        navBarAppearanceController.appearanceFlow.collect { appearance ->
            val color = appearance?.color?.toArgb() ?: defaultNavBarColor
            val isLight = appearance?.isLight ?: isDefaultNavBarLight
            view.changeNavigationBar(
                color = color,
                isLight = isLight,
            )
        }
    }
}