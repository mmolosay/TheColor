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
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.settings.SettingsData
import io.github.mmolosay.thecolor.presentation.settings.SettingsUiStrings
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel
import io.github.mmolosay.thecolor.presentation.settings.SettingsViewModel.DataState
import io.github.mmolosay.thecolor.domain.model.UserPreferences.ColorInputType as DomainColorInputType

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
    LargeTopAppBar(
        title = {
            Text(text = strings.topBarTitle)
        },
        navigationIcon = {
            IconButton(
                onClick = navigateBack,
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
        item("preferred color input") {
            var showSelectionDialog by remember { mutableStateOf(false) }
            val options = DomainColorInputType.entries.map { colorInputType ->
                ColorInputOption(
                    name = colorInputType.toUiString(strings),
                    isSelected = (data.preferredColorInputType == colorInputType),
                    onSelect = { data.changePreferredColorInputType(colorInputType) },
                )
            }
            PreferredColorInput(
                title = strings.itemPreferredColorInputTitle,
                description = strings.itemPreferredColorInputDesc,
                selectedOption = data.preferredColorInputType.toUiString(strings),
                onClick = { showSelectionDialog = true },
            )
            if (showSelectionDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                ) {
                    PreferredColorInputOptionSelection(options = options)
                }
            }
        }
    }
}

private fun DomainColorInputType.toUiString(
    strings: SettingsUiStrings,
): String =
    when (this) {
        DomainColorInputType.Hex -> strings.itemPreferredColorInputValueHex
        DomainColorInputType.Rgb -> strings.itemPreferredColorInputValueRgb
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
    )