package io.github.mmolosay.thecolor

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.HomeScreen
import io.github.mmolosay.thecolor.presentation.home.HomeViewModel
import io.github.mmolosay.thecolor.presentation.navigation.NavDest
import io.github.mmolosay.thecolor.presentation.navigation.Navigator
import io.github.mmolosay.thecolor.presentation.navigation.handle
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var navigator: Navigator

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        setContent {
            TheColorTheme {
                Application(
                    navigator = navigator,
                )
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
private fun Application(
    navigator: Navigator,
) {
    val navController = rememberNavController()
    AppNavHost(
        navController = navController,
    )
    LaunchedEffect(Unit) {
        navigator.navEventFlow.collect { navEvent ->
            navController handle navEvent
        }
    }

}

@Composable
private fun AppNavHost(
    navController: NavHostController,
) =
    NavHost(
        navController = navController,
        startDestination = NavDest.Home.route,
    ) {
        home()
        settings()
    }

private fun NavGraphBuilder.home() =
    composable(route = NavDest.Home.route) {
        val homeViewModel: HomeViewModel = hiltViewModel()
        HomeScreen(
            vm = homeViewModel,
        )
    }

private fun NavGraphBuilder.settings() =
    composable(route = NavDest.Settings.route) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center,
        ) {
            Text(text = "Settings")
        }
    }