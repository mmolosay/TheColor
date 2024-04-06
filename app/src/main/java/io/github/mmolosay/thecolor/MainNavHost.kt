package io.github.mmolosay.thecolor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.mmolosay.thecolor.presentation.home.HomeScreen
import io.github.mmolosay.thecolor.presentation.home.HomeViewModel
import io.github.mmolosay.thecolor.presentation.navigation.NavDest

/**
 * A top-level [NavHost] of the entire application.
 */
@Composable
internal fun MainNavHost(
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