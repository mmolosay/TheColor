package io.github.mmolosay.thecolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.DomainUiThemeModeResolver.resolve
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultNavigationBarColor
import io.github.mmolosay.thecolor.presentation.design.LocalIsDefaultNavigationBarLight
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.changeNavigationBar
import io.github.mmolosay.thecolor.presentation.impl.toArgb

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val viewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            val uiTheme = viewModel.appUiThemeModeFlow
                .collectAsStateWithLifecycle(initialValue = null).value
                ?.resolve(isSystemInDarkMode = isSystemInDarkTheme())
                ?: return@setContent
            TheColorTheme(
                theme = uiTheme,
            ) {
                Application()
            }
        }
    }

    private fun enableEdgeToEdge() =
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )
}

@Composable
private fun Application() {
    val navController = rememberNavController()
    val navBarAppearanceController = remember { NavBarAppearanceController() }
    val view = LocalView.current

    MainNavHost(
        navController = navController,
        navBarAppearanceController = navBarAppearanceController,
    )

    val backStackEntry by navController.currentBackStackEntryAsState()
    val defaultNavBarColor = LocalDefaultNavigationBarColor
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