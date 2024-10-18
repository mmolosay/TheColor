package io.github.mmolosay.thecolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalView
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultNavigationBarColor
import io.github.mmolosay.thecolor.presentation.design.LocalIsDefaultNavigationBarLight
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.systemBrightness
import io.github.mmolosay.thecolor.presentation.impl.changeNavigationBar
import io.github.mmolosay.thecolor.presentation.impl.toArgb
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private var splashScreen: SplashScreen? = null

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        setSplashScreen()
        collectSplashState()
        super.onCreate(savedInstanceState)
        setContent { Content() }
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

    private fun setSplashScreen() {
        val splashScreen = installSplashScreen()
        this.splashScreen = splashScreen
        splashScreen.setKeepOnScreenCondition { true }
    }

    private fun collectSplashState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                splashViewModel.isWorkCompleteFlow.collect { isWorkComplete ->
                    if (!isWorkComplete) return@collect
                    run dismissAndClearSplashScreen@{
                        val splashScreen = requireNotNull(splashScreen)
                        splashScreen.setKeepOnScreenCondition { false }
                        this@MainActivity.splashScreen = null
                    }
                    cancel() // once splash phase is passed, cancel collection of splash state
                }
            }
        }
    }

    @Composable
    private fun Content() {
        val colorScheme = mainViewModel.appUiColorSchemeResolverFlow
            .collectAsStateWithLifecycle(initialValue = null).value
            ?.resolve(brightness = systemBrightness())
            ?: return

        TheColorTheme(
                colorScheme = colorScheme,
            ) {
                Application()
            }
        }
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