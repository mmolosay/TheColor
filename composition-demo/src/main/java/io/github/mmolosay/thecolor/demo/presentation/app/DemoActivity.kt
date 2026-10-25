package io.github.mmolosay.thecolor.demo.presentation.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.demo.data.FakeDemoSettings
import io.github.mmolosay.thecolor.demo.presentation.feature.recipe.RecipeScreen
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import javax.inject.Inject

@AndroidEntryPoint
class DemoActivity : ComponentActivity() {

    // created when first shown, as the screen is, so that the read starts with the screen
    @Inject
    lateinit var settings: Lazy<FakeDemoSettings>

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TheColorTheme {
                Surface {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                    ) {
                        SettingsBar(
                            settings = settings.get(),
                            modifier = Modifier.statusBarsPadding(),
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .consumeWindowInsets(WindowInsets.statusBars),
                        ) {
                            RecipeScreen()
                        }
                    }
                }
            }
        }
    }
}
