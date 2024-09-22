package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.home.HomeScreen
import io.github.mmolosay.thecolor.presentation.home.HomeViewModel
import io.github.mmolosay.thecolor.presentation.settings.Settings
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel

/**
 * A top-level [NavHost] of the entire application.
 */
@Composable
internal fun MainNavHost(
    navController: NavHostController,
    navBarAppearanceController: NavBarAppearanceController,
) =
    NavHost(
        navController = navController,
        startDestination = AppNavDest.Home.route,
    ) {
        home(
            mainNavController = navController,
            navBarAppearanceController = navBarAppearanceController,
        )
        settings(
            mainNavController = navController,
        )
    }

private fun NavGraphBuilder.home(
    mainNavController: NavController,
    navBarAppearanceController: NavBarAppearanceController,
) =
    composable(route = AppNavDest.Home.route) {
        val homeViewModel: HomeViewModel = hiltViewModel()
        HomeScreen(
            viewModel = homeViewModel,
            navigateToSettings = {
                mainNavController.navigate(route = AppNavDest.Settings.route)
            },
            navBarAppearanceController = navBarAppearanceController,
        )
    }

private fun NavGraphBuilder.settings(
    mainNavController: NavController,
) =
    composable(route = AppNavDest.Settings.route) {
        val settingsViewModel: SettingsViewModel = hiltViewModel()
        Settings(
            viewModel = settingsViewModel,
            navigateToHome = {
                val previous = mainNavController.previousBackStackEntry
                if (previous?.destination?.route == AppNavDest.Home.route) {
                    mainNavController.popBackStack()
                } else {
                    mainNavController.navigate(route = AppNavDest.Home.route)
                }
            },
        )
    }