package io.github.mmolosay.thecolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultNavigationBarColor
import io.github.mmolosay.thecolor.presentation.design.LocalIsDefaultNavigationBarLight
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.changeNavigationBar
import io.github.mmolosay.thecolor.presentation.impl.toArgb

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TheColorTheme {
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
    if (backStackEntry != null) {
        // Restore nav bar when screen changes.
        // If the particular screen wants to style nav bar in its own way, it may do so.
        view.changeNavigationBar(
            navigationBarColor = LocalDefaultNavigationBarColor,
            isAppearanceLightNavigationBars = LocalIsDefaultNavigationBarLight.current,
        )
    }

    LaunchedEffect(Unit) {
        navBarAppearanceController.appearanceFlow.collect { appearance ->
            val navigationBarColor = appearance?.color?.toArgb() ?: Color.TRANSPARENT
            val isAppearanceLightNavigationBars = appearance?.isLight ?: true
            view.changeNavigationBar(
                navigationBarColor = navigationBarColor,
                isAppearanceLightNavigationBars = isAppearanceLightNavigationBars,
            )
        }
    }
}