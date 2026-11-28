package io.github.mmolosay.thecolor.presentation.settings.ui

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.asSingletonSet
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.isSingleton
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.single
import io.github.mmolosay.thecolor.presentation.common.compose.onlyBottom
import io.github.mmolosay.thecolor.presentation.common.compose.withoutBottom
import io.github.mmolosay.thecolor.presentation.design.Material3DynamicColorsAvailability.areDynamicColorsAvailable
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.settings.viewmodel.SettingsViewData
import io.github.mmolosay.thecolor.presentation.settings.viewmodel.SettingsViewModel
import kotlinx.collections.immutable.persistentListOf
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.domain.color.ColorInputType as DomainColorInputType
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorScheme as DomainUiColorScheme
import io.github.mmolosay.thecolor.domain.user.preferences.UserPreferences.UiColorSchemeSet as DomainUiColorSchemeSet
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navigateBack: () -> Unit,
    navigateToDevOptions: () -> Unit,
    navigateToAcknowledgements: () -> Unit,
) {
    val context = LocalContext.current
    val strings = remember(context) { SettingsUiStrings(context) }
    val data = viewModel.viewData.collectAsStateWithLifecycle().value
    if (data == null) {
        // should promptly become non-null, don't show loading indicator to avoid flashing
        Box(
            modifier = Modifier.fillMaxSize(),
        )
    } else {
        SettingsScreen(
            data = data,
            strings = strings,
            onPreferredColorInputTypeChange = viewModel::changePreferredColorInputType,
            onAppUiColorSchemeSetChange = viewModel::changeAppUiColorSchemeSet,
            onDynamicUiColorsChange = viewModel::changeDynamicUiColors,
            onResumeFromLastSearchedColorChange = viewModel::changeResumeFromLastSearchedColor,
            onSmartBackspaceChange = viewModel::changeSmartBackspace,
            onSelectAllTextOnTextFieldFocusChange = viewModel::changeSelectAllTextOnTextFieldFocus,
            onAutoProceedWithRandomizedColorsChange = viewModel::changeAutoProceedWithRandomizedColors,
            onResetToDefaultsConfirm = viewModel::resetToDefaults,
            navigateBack = navigateBack,
            navigateToDevOptions = navigateToDevOptions,
            navigateToAcknowledgements = navigateToAcknowledgements,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    data: SettingsViewData,
    strings: SettingsUiStrings,
    onPreferredColorInputTypeChange: (DomainColorInputType) -> Unit,
    onAppUiColorSchemeSetChange: (DomainUiColorSchemeSet) -> Unit,
    onDynamicUiColorsChange: (Boolean) -> Unit,
    onResumeFromLastSearchedColorChange: (Boolean) -> Unit,
    onSmartBackspaceChange: (Boolean) -> Unit,
    onSelectAllTextOnTextFieldFocusChange: (Boolean) -> Unit,
    onAutoProceedWithRandomizedColorsChange: (Boolean) -> Unit,
    onResetToDefaultsConfirm: () -> Unit,
    navigateBack: () -> Unit,
    navigateToDevOptions: () -> Unit,
    navigateToAcknowledgements: () -> Unit,
) {
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showResetPreferencesToDefaultDialog by remember { mutableStateOf(false) }
    fun dismissResetPreferencesToDefaultDialog() {
        showResetPreferencesToDefaultDialog = false
    }
    if (showResetPreferencesToDefaultDialog) {
        ResetValuesToDefaultAlertDialog(
            onDismissRequest = ::dismissResetPreferencesToDefaultDialog,
            strings = strings,
            onConfirmClick = {
                onResetToDefaultsConfirm()
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
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.withoutBottom(),
    ) { contentPadding ->
        Settings(
            modifier = Modifier
                .padding(contentPadding)
                // consuming 'contentPadding' as window insets isn't needed here
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            data = data,
            strings = strings,
            onPreferredColorInputTypeChange = onPreferredColorInputTypeChange,
            onAppUiColorSchemeSetChange = onAppUiColorSchemeSetChange,
            onDynamicUiColorsChange = onDynamicUiColorsChange,
            onResumeFromLastSearchedColorChange = onResumeFromLastSearchedColorChange,
            onSmartBackspaceChange = onSmartBackspaceChange,
            onSelectAllTextOnTextFieldFocusChange = onSelectAllTextOnTextFieldFocusChange,
            onAutoProceedWithRandomizedColorsChange = onAutoProceedWithRandomizedColorsChange,
            navigateToDevOptions = navigateToDevOptions,
            navigateToAcknowledgements = navigateToAcknowledgements,
            extraContentPadding = ScaffoldDefaults.contentWindowInsets.onlyBottom()
                .asPaddingValues(),
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
                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_arrow_back),
                    contentDescription = strings.topBarGoBackIconDesc,
                )
            }
        },
        actions = {
            IconButton(
                onClick = onResetPreferencesToDefaultClick,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_reset_settings),
                    contentDescription = strings.topBarResetPreferencesToDefaultIconDesc,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun Settings(
    data: SettingsViewData,
    strings: SettingsUiStrings,
    onPreferredColorInputTypeChange: (DomainColorInputType) -> Unit,
    onAppUiColorSchemeSetChange: (DomainUiColorSchemeSet) -> Unit,
    onDynamicUiColorsChange: (Boolean) -> Unit,
    onResumeFromLastSearchedColorChange: (Boolean) -> Unit,
    onSmartBackspaceChange: (Boolean) -> Unit,
    onSelectAllTextOnTextFieldFocusChange: (Boolean) -> Unit,
    onAutoProceedWithRandomizedColorsChange: (Boolean) -> Unit,
    navigateToDevOptions: () -> Unit,
    navigateToAcknowledgements: () -> Unit,
    modifier: Modifier = Modifier,
    extraContentPadding: PaddingValues = PaddingValues.Zero,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = extraContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        item("preferred color input type") {
            var showSelectionDialog by remember { mutableStateOf(false) }
            val options = DomainColorInputType.entries.map { type ->
                ColorInputTypeOption(
                    text = type.toUiString(strings),
                    isSelected = (data.preferredColorInputType == type),
                    onSelect = { onPreferredColorInputTypeChange(type) },
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
                    PreferredColorInputTypeSelection(
                        options = options,
                    )
                }
            }
        }

        item("app ui color scheme") {
            var showSelectionDialog by remember { mutableStateOf(false) }
            val options = data.supportedAppUiColorSchemeSets.map { set ->
                AppUiColorSchemeOption(
                    text = set.toVerboseUiString(strings),
                    isSelected = (data.appUiColorSchemeSet == set),
                    onSelect = { onAppUiColorSchemeSetChange(set) },
                )
            }
            AppUiColorScheme(
                title = strings.itemAppUiColorSchemeTitle,
                description = strings.itemAppUiColorSchemeDesc,
                value = data.appUiColorSchemeSet.toShortUiString(strings),
                onClick = { showSelectionDialog = true },
            )
            if (showSelectionDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                ) {
                    AppUiColorSchemeSelection(
                        options = options,
                    )
                }
            }
        }

        if (areDynamicColorsAvailable()) {
            item("dynamic ui colors") {
                DynamicUiColors(
                    title = strings.itemDynamicUiColorsTitle,
                    description = strings.itemDynamicUiColorsDesc,
                    checked = data.isDynamicUiColorsEnabled,
                    onCheckedChange = onDynamicUiColorsChange,
                )
            }
        }

        item("resume from last searched color") {
            ResumeFromLastSearchedColor(
                title = strings.itemResumeFromLastSearchedColorTitle,
                description = strings.itemResumeFromLastSearchedColorDesc,
                checked = data.isResumeFromLastSearchedColorEnabled,
                onCheckedChange = onResumeFromLastSearchedColorChange,
            )
        }

        item("smart backspace") {
            SmartBackspace(
                title = strings.itemSmartBackspaceTitle,
                description = strings.itemSmartBackspaceDesc,
                checked = data.isSmartBackspaceEnabled,
                onCheckedChange = onSmartBackspaceChange,
            )
        }

        item("select all text on text field focus") {
            SelectAllTextOnTextFieldFocus(
                title = strings.itemSelectAllTextOnTextFieldFocusTitle,
                description = strings.itemSelectAllTextOnTextFieldFocusDesc,
                checked = data.isSelectAllTextOnTextFieldFocusEnabled,
                onCheckedChange = onSelectAllTextOnTextFieldFocusChange,
            )
        }

        item("auto proceed with randomized colors") {
            AutoProceedWithRandomizedColors(
                title = strings.itemAutoProceedWithRandomizedColorsTitle,
                description = strings.itemAutoProceedWithRandomizedColorsDesc,
                checked = data.isAutoProceedWithRandomizedColorsEnabled,
                onCheckedChange = onAutoProceedWithRandomizedColorsChange,
            )
        }

        item("acknowledgements") {
            Acknowledgements(
                title = "Acknowledgements",
                onClick = navigateToAcknowledgements,
            )
        }

        // keep this item very last
        if (data.isDevOptionsEnabled) {
            item("dev options") {
                DevOptions(
                    title = strings.itemDevOptionsTitle,
                    onClick = navigateToDevOptions,
                )
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
        DomainColorInputType.Hsv -> strings.itemPreferredColorInputTypeValueHsv
    }

private fun DomainUiColorSchemeSet.toShortUiString(
    strings: SettingsUiStrings,
): String =
    if (this.isSingleton()) {
        when (this.single()) {
            DomainUiColorScheme.Light -> strings.itemAppUiColorSchemeValueLight
            DomainUiColorScheme.Dark -> strings.itemAppUiColorSchemeValueDark
            DomainUiColorScheme.Jungle -> strings.itemAppUiColorSchemeValueJungle
            DomainUiColorScheme.Midnight -> strings.itemAppUiColorSchemeValueMidnight
        }
    } else {
        when (this) {
            DomainUiColorSchemeSet.DayNight -> strings.itemAppUiColorSchemeValueDayNightShort
            else -> error("Unsupported UI color scheme set")
        }
    }

private fun DomainUiColorSchemeSet.toVerboseUiString(
    strings: SettingsUiStrings,
): String =
    if (this.isSingleton()) {
        when (this.single()) {
            DomainUiColorScheme.Light -> strings.itemAppUiColorSchemeValueLight
            DomainUiColorScheme.Dark -> strings.itemAppUiColorSchemeValueDark
            DomainUiColorScheme.Jungle -> strings.itemAppUiColorSchemeValueJungle
            DomainUiColorScheme.Midnight -> strings.itemAppUiColorSchemeValueMidnight
        }
    } else {
        when (this) {
            DomainUiColorSchemeSet.DayNight -> strings.itemAppUiColorSchemeValueDayNightVerbose
            else -> error("Unsupported UI color scheme set")
        }
    }

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        SettingsScreen(
            data = previewData(),
            strings = SettingsUiStrings(LocalContext.current),
            onPreferredColorInputTypeChange = {},
            onAppUiColorSchemeSetChange = {},
            onDynamicUiColorsChange = {},
            onResumeFromLastSearchedColorChange = {},
            onSmartBackspaceChange = {},
            onSelectAllTextOnTextFieldFocusChange = {},
            onAutoProceedWithRandomizedColorsChange = {},
            onResetToDefaultsConfirm = {},
            navigateBack = {},
            navigateToDevOptions = {},
            navigateToAcknowledgements = {},
        )
    }
}

private fun previewData() =
    SettingsViewData(
        preferredColorInputType = DomainColorInputType.Hex,
        appUiColorSchemeSet = DomainUiColorSchemeSet.DayNight,
        supportedAppUiColorSchemeSets = persistentListOf(
            DomainUiColorScheme.Light.asSingletonSet(),
            DomainUiColorScheme.Dark.asSingletonSet(),
            DomainUiColorSchemeSet.DayNight,
        ),
        isDynamicUiColorsEnabled = true,
        isResumeFromLastSearchedColorEnabled = true,
        isSmartBackspaceEnabled = true,
        isSelectAllTextOnTextFieldFocusEnabled = true,
        isAutoProceedWithRandomizedColorsEnabled = true,
        isDevOptionsEnabled = true,
    )