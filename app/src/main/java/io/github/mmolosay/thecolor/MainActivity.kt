package io.github.mmolosay.thecolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.design.Brightness
import io.github.mmolosay.thecolor.presentation.design.ColorScheme
import io.github.mmolosay.thecolor.presentation.design.LocalDefaultShouldUseLightTintForNavBarControls
import io.github.mmolosay.thecolor.presentation.design.Material3DynamicColorsAvailability.areDynamicColorsAvailable
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.animateColors
import io.github.mmolosay.thecolor.presentation.design.brightness
import io.github.mmolosay.thecolor.presentation.design.shouldUseLightTintForNavBarControls
import io.github.mmolosay.thecolor.presentation.design.systemBrightness
import io.github.mmolosay.thecolor.presentation.design.toMaterialColorScheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        var isContentComposed = false
        enableInitialEdgeToEdge()
        installSplashScreen().setKeepOnScreenCondition { !isContentComposed }
        super.onCreate(savedInstanceState)
        window.setBackgroundDrawable(null)
        val splashViewModel = ViewModelProvider(this)[SplashViewModel::class]
        setContent {
            val isSplashWorkComplete = splashViewModel.isWorkCompleteFlow
                .collectAsStateWithLifecycle().value
            if (!isSplashWorkComplete) return@setContent
            val colorScheme = run {
                val areDynamicColorsEnabled = mainViewModel.flowOfDynamicUiColors
                    .collectAsStateWithLifecycle(initialValue = null).value
                    ?.enabled
                val useDynamicColorSchemes =
                    (areDynamicColorsAvailable() && (areDynamicColorsEnabled ?: false))
                mainViewModel.flowOfAppUiColorSchemeResolver
                    .collectAsStateWithLifecycle(initialValue = null).value
                    ?.resolve(
                        brightness = systemBrightness(),
                        useDynamicColorSchemes = useDynamicColorSchemes,
                    )
            } ?: return@setContent

            Content(colorScheme)
            SideEffect { isContentComposed = true }
        }
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

    @Composable
    private fun Content(colorScheme: ColorScheme) {
        val context = LocalContext.current

        LaunchedEffect(colorScheme) {
            enableEdgeToEdge(colorSchemeBrightness = colorScheme.brightness())
        }

        val targetMaterialColorScheme = remember(colorScheme) { colorScheme.toMaterialColorScheme(context) }
        val animatedMaterialColorScheme = targetMaterialColorScheme.animateColors()

        val useLightTintForNavBarControls = remember(colorScheme) {
            colorScheme.shouldUseLightTintForNavBarControls()
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