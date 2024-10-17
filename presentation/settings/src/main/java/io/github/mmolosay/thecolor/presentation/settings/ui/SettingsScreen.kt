package io.github.mmolosay.thecolor.presentation.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.domain.model.UserPreferences
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.settings.SettingsData
import io.github.mmolosay.thecolor.presentation.settings.SettingsUiStrings
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel.DataState
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiTheme as DomainUiTheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiThemeMode as DomainUiThemeMode

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navigateBack: () -> Unit,
) {
    val dataState by viewModel.dataStateFlow.collectAsStateWithLifecycle()
    SettingsScreen(
        dataState = dataState,
        navigateBack = navigateBack,
    )
}

@Composable
fun SettingsScreen(
    dataState: DataState,
    navigateBack: () -> Unit,
) {
    when (dataState) {
        is DataState.Loading -> {
            // should promptly change to 'Ready', don't show loading indicator to avoid flashing
            Box(
                modifier = Modifier.fillMaxSize(),
            )
        }
        is DataState.Ready -> {
            SettingsScreen(
                data = dataState.data,
                navigateBack = navigateBack
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    data: SettingsData,
    navigateBack: () -> Unit,
) {
    val strings = SettingsUiStrings(LocalContext.current)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    Scaffold(
        topBar = {
            TopBar(
                strings = strings,
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
            )
        },
    ) { contentPadding ->
        Settings(
            modifier = Modifier
                .padding(contentPadding)
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            data = data,
            strings = strings,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    strings: SettingsUiStrings,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
) {
    // TODO: add an action to reset settings to default?
    val debouncedNavigateBack = remember(navigateBack) {
        debounced(
            action = navigateBack,
            timeout = 1000.milliseconds,
        )
    }
    LargeTopAppBar(
        title = {
            Text(text = strings.topBarTitle)
        },
        navigationIcon = {
            IconButton(
                onClick = debouncedNavigateBack,
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Rounded.ArrowBack,
                    contentDescription = strings.topBarGoBackIconDesc,
                )
            }
        },
        colors = TopAppBarDefaults.largeTopAppBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    data: SettingsData,
    strings: SettingsUiStrings,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("preferred color input type") {
            var showSelectionDialog by remember { mutableStateOf(false) }
            val options = DomainColorInputType.entries.map { colorInputType ->
                ColorInputTypeOption(
                    name = colorInputType.toUiString(strings),
                    isSelected = (data.preferredColorInputType == colorInputType),
                    onSelect = { data.changePreferredColorInputType(colorInputType) },
                )
            }
            PreferredColorInputType(
                title = strings.itemPreferredColorInputTypeTitle,
                description = strings.itemPreferredColorInputTypeDesc,
                value = data.preferredColorInputType.toUiString(strings),
                onClick = { showSelectionDialog = true },
            )
            if (showSelectionDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                ) {
                    PreferredColorInputTypeSelection(options = options)
                }
            }
        }

        item("app ui theme") {
            var showSelectionDialog by remember { mutableStateOf(false) }
            val options = data.supportedAppUiThemeModes.map { mode ->
                AppUiThemeOption(
                    name = mode.toVerboseUiString(strings),
                    isSelected = (data.appUiThemeMode == mode),
                    onSelect = { data.changeAppUiThemeMode(mode) },
                )
            }
            AppUiTheme(
                title = strings.itemAppUiThemeTitle,
                value = data.appUiThemeMode.toShortUiString(strings),
                onClick = { showSelectionDialog = true },
            )
            if (showSelectionDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                ) {
                    AppUiThemeSelection(options = options)
                }
            }
        }
    }
}

private fun DomainColorInputType.toUiString(
    strings: SettingsUiStrings,
): String =
    when (this) {
        DomainColorInputType.Hex -> strings.itemPreferredColorInputTypeValueHex
        DomainColorInputType.Rgb -> strings.itemPreferredColorInputTypeValueRgb
    }

private fun DomainUiThemeMode.toShortUiString(
    strings: SettingsUiStrings,
): String =
    when (this) {
        is UserPreferences.UiThemeMode.Single -> {
            when (this.theme) {
                UserPreferences.UiTheme.Light -> strings.itemAppUiThemeValueLight
                UserPreferences.UiTheme.Dark -> strings.itemAppUiThemeValueDark
            }
        }
        is UserPreferences.UiThemeMode.Dual -> {
            when {
                this == DomainUiThemeMode.DayNight -> strings.itemAppUiThemeValueDayNightShort
                else -> error("Unsupported UI theme mode")
            }
        }
    }

private fun DomainUiThemeMode.toVerboseUiString(
    strings: SettingsUiStrings,
): String =
    when (this) {
        is UserPreferences.UiThemeMode.Single -> {
            when (this.theme) {
                UserPreferences.UiTheme.Light -> strings.itemAppUiThemeValueLight
                UserPreferences.UiTheme.Dark -> strings.itemAppUiThemeValueDark
            }
        }
        is UserPreferences.UiThemeMode.Dual -> {
            when {
                this == DomainUiThemeMode.DayNight -> strings.itemAppUiThemeValueDayNightVerbose
                else -> error("Unsupported UI theme mode")
            }
        }
    }

@Preview
@Composable
private fun Preview() {
    TheColorTheme {
        SettingsScreen(
            data = previewData(),
            navigateBack = {},
        )
    }
}

private fun previewData() =
    SettingsData(
        preferredColorInputType = DomainColorInputType.Hex,
        changePreferredColorInputType = {},
        appUiThemeMode = DomainUiThemeMode.DayNight,
        supportedAppUiThemeModes = listOf(
            DomainUiThemeMode.Single(theme = DomainUiTheme.Light),
            DomainUiThemeMode.Single(theme = DomainUiTheme.Dark),
            DomainUiThemeMode.DayNight,
        ),
        changeAppUiThemeMode = {},
    )