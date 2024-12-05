package io.github.mmolosay.thecolor.presentation.settings.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.RestartAlt
import androidx.compose.material3.BottomSheetDefaults
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
import io.github.mmolosay.thecolor.domain.model.UserPreferences.asSingletonSet
import io.github.mmolosay.thecolor.domain.model.UserPreferences.isSingleton
import io.github.mmolosay.thecolor.domain.model.UserPreferences.single
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.onlyBottom
import io.github.mmolosay.thecolor.presentation.impl.withoutBottom
import io.github.mmolosay.thecolor.presentation.settings.SettingsData
import io.github.mmolosay.thecolor.presentation.settings.SettingsUiStrings
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel.DataState
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.domain.model.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorScheme as DomainUiColorScheme
import io.github.mmolosay.thecolor.domain.model.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet

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
    var showResetPreferencesToDefaultDialog by remember { mutableStateOf(false) }
    val dismissResetPreferencesToDefaultDialog: () -> Unit = {
        showResetPreferencesToDefaultDialog = false
    }

    if (showResetPreferencesToDefaultDialog) {
        ResetPreferencesToDefaultAlertDialog(
            onDismissRequest = dismissResetPreferencesToDefaultDialog,
            strings = strings,
            onConfirmClick = {
                data.resetPreferencesToDefault()
                dismissResetPreferencesToDefaultDialog()
            },
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                strings = strings,
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
                onResetPreferencesToDefaultClick = { showResetPreferencesToDefaultDialog = true },
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
    onResetPreferencesToDefaultClick: () -> Unit,
) {
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
        actions = {
            IconButton(
                onClick = onResetPreferencesToDefaultClick,
            ) {
                Icon(
                    imageVector = Icons.Rounded.RestartAlt,
                    contentDescription = strings.topBarResetPreferencesToDefaultIconDesc,
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
                val windowInsets = BottomSheetDefaults.windowInsets
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                    windowInsets = windowInsets.withoutBottom(),
                ) {
                    val bottomWindowInsets = windowInsets.onlyBottom()
                    PreferredColorInputTypeSelection(
                        modifier = Modifier
                            .padding(bottomWindowInsets.asPaddingValues())
                            .consumeWindowInsets(bottomWindowInsets),
                        options = options,
                    )
                }
            }
        }

        item("app ui color scheme") {
            var showSelectionDialog by remember { mutableStateOf(false) }
            val options = data.supportedAppUiColorSchemeSets.map { mode ->
                AppUiColorSchemeOption(
                    name = mode.toVerboseUiString(strings),
                    isSelected = (data.appUiColorSchemeSet == mode),
                    onSelect = { data.changeAppUiColorSchemeSet(mode) },
                )
            }
            AppUiColorScheme(
                title = strings.itemAppUiColorSchemeTitle,
                description = strings.itemAppUiColorSchemeDesc,
                value = data.appUiColorSchemeSet.toShortUiString(strings),
                onClick = { showSelectionDialog = true },
            )
            if (showSelectionDialog) {
                val windowInsets = BottomSheetDefaults.windowInsets
                val bottomWindowInsets = windowInsets.onlyBottom()
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                    windowInsets = windowInsets.withoutBottom(),
                ) {
                    AppUiColorSchemeSelection(
                        modifier = Modifier
                            .padding(bottomWindowInsets.asPaddingValues())
                            .consumeWindowInsets(bottomWindowInsets),
                        options = options,
                    )
                }
            }
        }

        item("resume from last searched color") {
            ResumeFromLastSearchedColor(
                title = strings.itemResumeFromLastSearchedColorTitle,
                description = strings.itemResumeFromLastSearchedColorDesc,
                checked = data.isResumeFromLastSearchedColorOnStartupEnabled,
                onCheckedChange = data.changeResumeFromLastSearchedColorOnStartupEnablement,
            )
        }

        item("smart backspace") {
            SmartBackspace(
                title = strings.itemSmartBackspaceTitle,
                description = strings.itemSmartBackspaceDesc,
                checked = data.isSmartBackspaceEnabled,
                onCheckedChange = data.changeSmartBackspaceEnablement,
            )
        }

        item("select all text on text field focus") {
            SelectAllTextOnTextFieldFocus(
                title = strings.itemSelectAllTextOnTextFieldFocusTitle,
                description = strings.itemSelectAllTextOnTextFieldFocusDesc,
                checked = data.isSelectAllTextOnTextFieldFocusEnabled,
                onCheckedChange = data.changeSelectAllTextOnTextFieldFocusEnablement,
            )
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

private fun DomainUiColorSchemeSet.toShortUiString(
    strings: SettingsUiStrings,
): String =
    if (this.isSingleton()) {
        when (this.single()) {
            DomainUiColorScheme.Light -> strings.itemAppUiColorSchemeValueLight
            DomainUiColorScheme.Dark -> strings.itemAppUiColorSchemeValueDark
        }
    } else {
        when (this) {
            DomainUiColorSchemeSet.DayNight -> strings.itemAppUiColorSchemeValueDayNightShort
            else -> error("Unsupported UI color scheme mode")
        }
    }

private fun DomainUiColorSchemeSet.toVerboseUiString(
    strings: SettingsUiStrings,
): String =
    if (this.isSingleton()) {
        when (this.single()) {
            DomainUiColorScheme.Light -> strings.itemAppUiColorSchemeValueLight
            DomainUiColorScheme.Dark -> strings.itemAppUiColorSchemeValueDark
        }
    } else {
        when (this) {
            DomainUiColorSchemeSet.DayNight -> strings.itemAppUiColorSchemeValueDayNightVerbose
            else -> error("Unsupported UI color scheme mode")
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
        resetPreferencesToDefault = {},

        preferredColorInputType = DomainColorInputType.Hex,
        changePreferredColorInputType = {},

        appUiColorSchemeSet = DomainUiColorSchemeSet.DayNight,
        supportedAppUiColorSchemeSets = listOf(
            DomainUiColorScheme.Light.asSingletonSet(),
            DomainUiColorScheme.Dark.asSingletonSet(),
            DomainUiColorSchemeSet.DayNight,
        ),
        changeAppUiColorSchemeSet = {},

        isResumeFromLastSearchedColorOnStartupEnabled = true,
        changeResumeFromLastSearchedColorOnStartupEnablement = {},

        isSmartBackspaceEnabled = true,
        changeSmartBackspaceEnablement = {},

        isSelectAllTextOnTextFieldFocusEnabled = true,
        changeSelectAllTextOnTextFieldFocusEnablement = {},
    )