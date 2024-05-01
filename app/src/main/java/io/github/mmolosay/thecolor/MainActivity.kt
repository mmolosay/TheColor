package io.github.mmolosay.thecolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.design.RestoreNavigationBarAsSideEffect
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

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

    val backStackEntry by navController.currentBackStackEntryAsState()
    if (backStackEntry != null) {
        // restore nav bar when screen changes.
        // If the particular screen wants to style nav bar in its own way, it may do so
        RestoreNavigationBarAsSideEffect()
    }

    MainNavHost(
        navController = navController,
    )
}