package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.mmolosay.thecolor.presentation.api.nav.bar.MainNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.home.HomeScreen
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel
import io.github.mmolosay.thecolor.presentation.settings.ui.SettingsScreen

/**
 * A top-level [NavHost] of the entire application.
 */
@Composable
internal fun MainNavHost(
    navController: NavHostController,
    mainNavBarAppearanceController: MainNavBarAppearanceController,
) =
    NavHost(
        navController = navController,
        startDestination = AppNavDest.Home.route,
    ) {
        home(
            mainNavController = navController,
            mainNavBarAppearanceController = mainNavBarAppearanceController,
        )
        settings(
            mainNavController = navController,
        )
    }

private fun NavGraphBuilder.home(
    mainNavController: NavController,
    mainNavBarAppearanceController: MainNavBarAppearanceController,
) =
    composable(route = AppNavDest.Home.route) {
        val homeViewModel: HomeViewModel = hiltViewModel()
        val childController = remember(mainNavBarAppearanceController) {
            val tag = it.destination.route.orEmpty()
            mainNavBarAppearanceController.branch(tag)
        }
        HomeScreen(
            viewModel = homeViewModel,
            navigateToSettings = {
                mainNavController.navigate(route = AppNavDest.Settings.route)
            },
            navBarAppearanceController = childController,
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