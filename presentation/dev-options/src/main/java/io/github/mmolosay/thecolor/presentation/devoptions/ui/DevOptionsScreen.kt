package io.github.mmolosay.thecolor.presentation.devoptions.ui

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
import io.github.mmolosay.thecolor.presentation.common.compose.onlyBottom
import io.github.mmolosay.thecolor.presentation.common.compose.withoutBottom
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.devoptions.DevOptionsData
import io.github.mmolosay.thecolor.presentation.devoptions.DevOptionsViewModel
import io.github.mmolosay.thecolor.presentation.devoptions.DevOptionsViewModel.DataState
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.domain.model.DevOptions.PredictableRandomColors as DomainPredictableRandomColors
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun DevOptionsScreen(
    viewModel: DevOptionsViewModel,
    navigateBack: () -> Unit,
) {
    val dataState by viewModel.dataStateFlow.collectAsStateWithLifecycle()
    DevOptionsScreen(
        dataState = dataState,
        navigateBack = navigateBack,
    )
}

@Composable
fun DevOptionsScreen(
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
            DevOptionsScreen(
                data = dataState.data,
                navigateBack = navigateBack,
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOptionsScreen(
    data: DevOptionsData,
    navigateBack: () -> Unit,
) {
    val strings = DevOptionsUiStrings(LocalContext.current)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    var showResetValuesToDefaultDialog by remember { mutableStateOf(false) }
    fun dismissResetValuesToDefaultDialog() {
        showResetValuesToDefaultDialog = false
    }
    if (showResetValuesToDefaultDialog) {
        ResetValuesToDefaultAlertDialog(
            onDismissRequest = ::dismissResetValuesToDefaultDialog,
            strings = strings,
            onConfirmClick = {
                data.resetValuesToDefault()
                dismissResetValuesToDefaultDialog()
            },
        )
    }

    Scaffold(
        topBar = {
            TopBar(
                strings = strings,
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
                onResetValuesToDefaultClick = { showResetValuesToDefaultDialog = true },
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.withoutBottom(),
    ) { contentPadding ->
        DevOptions(
            modifier = Modifier
                .padding(contentPadding)
                // consuming 'contentPadding' as window insets isn't needed here
                .nestedScroll(scrollBehavior.nestedScrollConnection),
            data = data,
            strings = strings,
            extraContentPadding = ScaffoldDefaults.contentWindowInsets.onlyBottom()
                .asPaddingValues(),
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    strings: DevOptionsUiStrings,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
    onResetValuesToDefaultClick: () -> Unit,
) {
    val debouncedNavigateBack = remember(navigateBack) {
        debounced(
            action = navigateBack,
            timeout = 1000.milliseconds,
        )
    }
    LargeTopAppBar(
        title = {
            // TODO: same style as on Settings screen. Extract into a reusable component?
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
                onClick = onResetValuesToDefaultClick,
            ) {
                Icon(
                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_restart_alt),
                    contentDescription = strings.topBarResetValuesToDefaultIconDesc,
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOptions(
    data: DevOptionsData,
    strings: DevOptionsUiStrings,
    modifier: Modifier = Modifier,
    extraContentPadding: PaddingValues = PaddingValues.Zero,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = extraContentPadding,
        verticalArrangement = Arrangement.spacedBy(12.dp), // TODO: same style as on Settings screen. Extract into a reusable component?
    ) {
        item("predictable random colors") {
            var showSelectionDialog by remember { mutableStateOf(false) }
            val options = DomainPredictableRandomColors.entries.map { predictableRandomColors ->
                PredictableRandomColorsOption(
                    text = predictableRandomColors.toVerboseUiString(strings),
                    isDefault = (predictableRandomColors == data.defaultPredictableRandomColors),
                    isSelected = (predictableRandomColors == data.predictableRandomColors),
                    onSelect = { data.changePredictableRandomColors(predictableRandomColors) },
                )
            }
            PredictableRandomColors(
                title = strings.itemPredictableRandomColorsTitle,
                description = strings.itemPredictableRandomColorsDesc,
                value = data.predictableRandomColors.toShortUiString(strings),
                showAttentionBadge = (data.predictableRandomColors != data.defaultPredictableRandomColors),
                onClick = { showSelectionDialog = true },
            )
            if (showSelectionDialog) {
                ModalBottomSheet(
                    onDismissRequest = { showSelectionDialog = false },
                ) {
                    PredictableRandomColorsOptionSelection(
                        options = options,
                    )
                }
            }
        }
    }
}

private fun DomainPredictableRandomColors.toShortUiString(
    strings: DevOptionsUiStrings,
): String =
    when (this) {
        DomainPredictableRandomColors.Random -> strings.itemPredictableRandomColorsValueRandom
        DomainPredictableRandomColors.CyclingRgb -> strings.itemPredictableRandomColorsValueCyclingRgbShort
        DomainPredictableRandomColors.CyclingLightDark -> strings.itemPredictableRandomColorsValueCyclingLightDarkShort
    }

private fun DomainPredictableRandomColors.toVerboseUiString(
    strings: DevOptionsUiStrings,
): String =
    when (this) {
        DomainPredictableRandomColors.Random -> strings.itemPredictableRandomColorsValueRandom
        DomainPredictableRandomColors.CyclingRgb -> strings.itemPredictableRandomColorsValueCyclingRgbVerbose
        DomainPredictableRandomColors.CyclingLightDark -> strings.itemPredictableRandomColorsValueCyclingLightDarkVerbose
    }

@Preview
@Composable
private fun Preview() {
    TheColorTheme {
        DevOptionsScreen(
            data = previewData(),
            navigateBack = {},
        )
    }
}

private fun previewData() =
    DevOptionsData(
        resetValuesToDefault = {},

        predictableRandomColors = DomainPredictableRandomColors.CyclingLightDark,
        defaultPredictableRandomColors = DomainPredictableRandomColors.Random,
        changePredictableRandomColors = {},
    )