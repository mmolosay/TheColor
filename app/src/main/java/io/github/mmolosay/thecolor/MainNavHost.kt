package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceStack
import io.github.mmolosay.thecolor.presentation.home.HomeScreen
import io.github.mmolosay.thecolor.presentation.home.HomeViewModel
import io.github.mmolosay.thecolor.presentation.settings.ui.SettingsScreen
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
            navBarAppearanceStack = navBarAppearanceController,
        )
        settings(
            mainNavController = navController,
        )
    }

private fun NavGraphBuilder.home(
    mainNavController: NavController,
    navBarAppearanceStack: NavBarAppearanceStack,
) =
    composable(route = AppNavDest.Home.route) {
        val homeViewModel: HomeViewModel = hiltViewModel()
        HomeScreen(
            viewModel = homeViewModel,
            navigateToSettings = {
                mainNavController.navigate(route = AppNavDest.Settings.route)
            },
            navBarAppearanceStack = navBarAppearanceStack,
        )
    }

private fun NavGraphBuilder.settings(
    mainNavController: NavController,
) =
    composable(route = AppNavDest.Settings.route) {
        val settingsViewModel: SettingsViewModel = hiltViewModel()
        SettingsScreen(
            viewModel = settingsViewModel,
            navigateBack = mainNavController::popBackStack,
        )
    }