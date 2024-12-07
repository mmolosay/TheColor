package io.github.mmolosay.thecolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.core.splashscreen.SplashScreen
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.design.Brightness
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultShouldUseLightTintForNavBarControls
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.animateColors
import io.github.mmolosay.thecolor.presentation.design.brightness
import io.github.mmolosay.thecolor.presentation.design.shouldUseLightTintForNavBarControls
import io.github.mmolosay.thecolor.presentation.design.systemBrightness
import io.github.mmolosay.thecolor.presentation.design.toMaterialColorScheme
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    private val splashViewModel: SplashViewModel by viewModels()
    private var splashScreen: SplashScreen? = null

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        enableInitialEdgeToEdge()
        setSplashScreen()
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        setContent { Content() }
        collectSplashState()
    }

    /**
     * An edge-to-edge appearance to be applied on initialization, when user-selected UI color scheme
     * is not fetched yet.
     * During app startup the splash screen will be shown,
     * which adheres to system dark mode, just as [SystemBarStyle.auto] does.
     */
    private fun enableInitialEdgeToEdge() =
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
            navigationBarStyle = SystemBarStyle.auto(
                lightScrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            ),
        )

    private fun enableEdgeToEdge(
        colorSchemeBrightness: Brightness,
    ) {
        val systemBarStyle = when (colorSchemeBrightness) {
            Brightness.Light -> SystemBarStyle.light(
                scrim = Color.TRANSPARENT,
                darkScrim = Color.TRANSPARENT,
            )
            Brightness.Dark -> SystemBarStyle.dark(
                scrim = Color.TRANSPARENT,
            )
        }
        enableEdgeToEdge(
            statusBarStyle = systemBarStyle,
            navigationBarStyle = systemBarStyle,
        )
    }

    private fun setSplashScreen() {
        val splashScreen = installSplashScreen()
        this.splashScreen = splashScreen
        splashScreen.setKeepOnScreenCondition { true }
    }

    private fun collectSplashState() {
        lifecycleScope.launch {
            splashViewModel.isWorkCompleteFlow.collect { isWorkComplete ->
                if (!isWorkComplete) return@collect

                val splashScreen = requireNotNull(splashScreen)
                splashScreen.setKeepOnScreenCondition { false }
                this@MainActivity.splashScreen = null

                cancel() // once splash phase has passed, cancel collection of splash state
            }
        }
    }

    @Composable
    private fun Content() {
        val colorScheme = mainViewModel.appUiColorSchemeResolverFlow
            .collectAsStateWithLifecycle(initialValue = null).value
            ?.resolve(brightness = systemBrightness())
            ?: return
        val useLightTintForNavBarControls = remember(colorScheme) {
            colorScheme.shouldUseLightTintForNavBarControls()
        }
        val materialColorScheme = colorScheme.toMaterialColorScheme()
        val animatedMaterialColorScheme = materialColorScheme.animateColors()

        LaunchedEffect(colorScheme) {
            enableEdgeToEdge(colorSchemeBrightness = colorScheme.brightness())
        }

        TheColorTheme(
            materialColorScheme = animatedMaterialColorScheme,
        ) {
            CompositionLocalProvider(
                LocalDefaultShouldUseLightTintForNavBarControls provides useLightTintForNavBarControls,
            ) {
                Application()
            }
        }
    }
}