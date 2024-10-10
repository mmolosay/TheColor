package io.github.mmolosay.thecolor.presentation.settings

import androidx.compose.foundation.layout.Arrangement
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
import io.github.mmolosay.thecolor.presentation.settings.UiItems.PreferredColorInput
import io.github.mmolosay.thecolor.presentation.settings.UiItems.PreferredColorInputSelection

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    navigateBack: () -> Unit,
) {
    SettingsScreen(
        data = viewModel.dataFlow.collectAsStateWithLifecycle().value,
        navigateBack = navigateBack,
    )
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
            val options = SettingsData.ColorInputType.entries.map { colorInputType ->
                UiItems.ColorInputOption(
                    name = colorInputType.toUiString(strings),
                    isSelected = (data.preferredColorInput == colorInputType),
                    onSelect = { data.changePreferredColorInput(colorInputType) },
                )
            }
            PreferredColorInput(
                title = strings.itemPreferredColorInputTitle,
                description = strings.itemPreferredColorInputDesc,
                selectedOption = data.preferredColorInput.toUiString(strings),
                onClick = { showSelectionDialog = true },
            )
            if (showSelectionDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                ) {
                    PreferredColorInputSelection(options = options)
                }
            }
        }
    }
}

private fun SettingsData.ColorInputType.toUiString(
    strings: SettingsUiStrings,
): String =
    when (this) {
        SettingsData.ColorInputType.Hex -> strings.itemPreferredColorInputValueHex
        SettingsData.ColorInputType.Rgb -> strings.itemPreferredColorInputValueRgb
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
        preferredColorInput = SettingsData.ColorInputType.Hex,
        changePreferredColorInput = {},
    )