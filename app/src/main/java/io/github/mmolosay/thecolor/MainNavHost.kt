package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import androidx.navigation.NavGraphBuilder
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.api.NavBarAppearanceStack
import io.github.mmolosay.thecolor.presentation.home.NavigateAwayFromHomeController
import io.github.mmolosay.thecolor.presentation.home.OnNavigateAwayFromHomeListener
import io.github.mmolosay.thecolor.presentation.home.HomeScreen
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
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
        val mapOfListeners = remember(mainNavController) {
            mutableMapOf<
                OnNavigateAwayFromHomeListener,
                NavController.OnDestinationChangedListener
            >()
        }
        val navigateAwayFromHomeController = remember(mainNavController) {
            NavigateAwayFromHomeControllerImpl(mainNavController, mapOfListeners)
        }
        HomeScreen(
            viewModel = homeViewModel,
            navigateToSettings = {
                mainNavController.navigate(route = AppNavDest.Settings.route)
            },
            navBarAppearanceStack = navBarAppearanceStack,
            navigateAwayFromHomeController = navigateAwayFromHomeController,
        )
    }

/**
 * An implementation of [NavigateAwayFromHomeController] that works with Compose Navigation library.
 */
private class NavigateAwayFromHomeControllerImpl(
    private val mainNavController: NavController,
    private val mapOfListeners: MutableMap<
        OnNavigateAwayFromHomeListener,
        NavController.OnDestinationChangedListener
    >,
) : NavigateAwayFromHomeController {

    override fun add(listener: OnNavigateAwayFromHomeListener) {
        val composeNavigationListener = listener.toComposeNavigationListener()
        mainNavController.addOnDestinationChangedListener(composeNavigationListener)
        mapOfListeners[listener] = composeNavigationListener
    }

    override fun remove(listener: OnNavigateAwayFromHomeListener) {
        val composeNavigationListener = mapOfListeners[listener] ?: return
        mainNavController.removeOnDestinationChangedListener(composeNavigationListener)
        mapOfListeners.remove(listener)
    }

    private fun OnNavigateAwayFromHomeListener.toComposeNavigationListener() =
        NavController.OnDestinationChangedListener l@{ _, destination, _ ->
            val destRoute = destination.route ?: return@l
            val appNavDest = AppNavDest.findByRoute(destRoute)
            val isNewDestNotAHome = (appNavDest != AppNavDest.Home)
            if (isNewDestNotAHome) {
                this.onNavigateAwayFromHome()
            }
        }
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