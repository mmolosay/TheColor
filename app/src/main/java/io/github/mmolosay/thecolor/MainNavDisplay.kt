package io.github.mmolosay.thecolor

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSerializable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.EntryProviderScope
import androidx.navigation3.runtime.NavBackStack
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import androidx.navigation3.ui.NavDisplay
import io.github.mmolosay.thecolor.presentation.acknowledgements.AcknowledgementsScreen
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.devoptions.DevOptionsViewModel
import io.github.mmolosay.thecolor.presentation.devoptions.ui.DevOptionsScreen
import io.github.mmolosay.thecolor.presentation.home.ui.HomeScreen
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewModel
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel
import io.github.mmolosay.thecolor.presentation.settings.ui.SettingsScreen
import kotlinx.serialization.serializer

@Composable
internal fun MainNavDisplay(
    rootNavBarAppearanceController: NavBarAppearanceController,
) {
    val backStack = rememberNavDestBackStack(NavDest.Home)
    NavDisplay(
        backStack = backStack,
        onBack = { backStack.removeLastOrNull() },
        entryDecorators = listOf(
            rememberSaveableStateHolderNavEntryDecorator(),
            rememberViewModelStoreNavEntryDecorator(),
        ),
        entryProvider = entryProvider {
            home(
                navigateToSettings = { backStack += NavDest.Settings },
                rootNavBarAppearanceController = rootNavBarAppearanceController,
            )
            settings(
                navigateBack = { backStack.removeLastOrNull() },
                navigateToDevOptions = { backStack += NavDest.DevOptions },
                navigateToAcknowledgements = { backStack += NavDest.Acknowledgements },
            )
            devOptions(
                navigateBack = { backStack.removeLastOrNull() },
            )
            acknowledgements(
                navigateBack = { backStack.removeLastOrNull() },
            )
        }
    )
}

@Composable
private fun rememberNavDestBackStack(
    vararg elements: NavDest,
): NavBackStack<NavDest> =
    rememberSerializable(serializer = serializer()) {
        NavBackStack(*elements)
    }

private fun EntryProviderScope<NavDest>.home(
    navigateToSettings: () -> Unit,
    rootNavBarAppearanceController: NavBarAppearanceController,
) =
    entry<NavDest.Home> {
        val homeViewModel: HomeViewModel = hiltViewModel()
        val childController = remember(rootNavBarAppearanceController) {
            val tag = NavDest.Home
            rootNavBarAppearanceController.branch(tag)
        }
        HomeScreen(
            viewModel = homeViewModel,
            navigateToSettings = navigateToSettings,
            navBarAppearanceController = childController,
        )
    }

private fun EntryProviderScope<NavDest>.settings(
    navigateBack: () -> Unit,
    navigateToDevOptions: () -> Unit,
    navigateToAcknowledgements: () -> Unit,
) =
    entry<NavDest.Settings> {
        val settingsViewModel: SettingsViewModel = hiltViewModel()
        SettingsScreen(
            viewModel = settingsViewModel,
            navigateBack = navigateBack,
            navigateToDevOptions = navigateToDevOptions,
            navigateToAcknowledgements = navigateToAcknowledgements,
        )
    }

private fun EntryProviderScope<NavDest>.devOptions(
    navigateBack: () -> Unit,
) =
    entry<NavDest.DevOptions> {
        val devOptionsViewModel: DevOptionsViewModel = hiltViewModel()
        DevOptionsScreen(
            viewModel = devOptionsViewModel,
            navigateBack = navigateBack,
        )
    }

private fun EntryProviderScope<NavDest>.acknowledgements(
    navigateBack: () -> Unit,
) =
    entry<NavDest.Acknowledgements> {
        AcknowledgementsScreen(
            navigateBack = navigateBack,
        )
    }