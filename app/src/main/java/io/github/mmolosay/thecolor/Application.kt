package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.rememberNavController
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearance
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultNavigationBarColor
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultShouldUseLightTintForNavBarControls
import io.github.mmolosay.thecolor.presentation.impl.changeNavigationBar
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

/**
 * A root of the entire application's UI.
 */
@Composable
internal fun Application() {
    val navController = rememberNavController()
    val navBarAppearanceController = rememberNavBarAppearanceController()
    val view = LocalView.current

    MainNavHost(
        navController = navController,
        navBarAppearanceController = navBarAppearanceController,
    )

    val defaultNavBarAppearance = NavBarAppearance(
        color = LocalDefaultNavigationBarColor.current
            .let { Optional.of(it) },
        useLightTintForControls = LocalDefaultShouldUseLightTintForNavBarControls.current
            .let { Optional.of(it) },
    )

    // change navigation bar when new appearance is emitted
    LaunchedEffect(Unit) changeNavigationBarWhenAppearanceChanges@{
        navBarAppearanceController.appearanceFlow.collect { appearanceWithTag ->
            val appearance = appearanceWithTag?.appearance ?: defaultNavBarAppearance
            view.changeNavigationBar(
                color = appearance.color.getOrNull(),
                useLightTintForControls = appearance.useLightTintForControls.getOrNull(),
            )
        }
    }
}

@Composable
private fun rememberNavBarAppearanceController(): NavBarAppearanceController {
    val coroutineScope = rememberCoroutineScope()
    return remember {
        NavBarAppearanceController(
            coroutineScope = coroutineScope,
        )
    }
}