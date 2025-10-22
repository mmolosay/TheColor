package io.github.mmolosay.thecolor.presentation.devoptions

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScaffoldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.debounce.debounced
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.impl.withoutBottom
import kotlin.time.Duration.Companion.milliseconds
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevOptionsScreen(
    navigateBack: () -> Unit,
) {
    val strings = DevOptionsUiStrings(LocalContext.current)
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()

    Scaffold(
        topBar = {
            TopBar(
                strings = strings,
                scrollBehavior = scrollBehavior,
                navigateBack = navigateBack,
//                onResetPreferencesToDefaultClick = { showResetPreferencesToDefaultDialog = true },
            )
        },
        contentWindowInsets = ScaffoldDefaults.contentWindowInsets.withoutBottom(),
    ) { contentPadding ->
        DevOptions(
            modifier = Modifier
                .padding(contentPadding)
                // consuming 'contentPadding' as window insets isn't needed here
                .nestedScroll(scrollBehavior.nestedScrollConnection),
//            data = data,
            strings = strings,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TopBar(
    strings: DevOptionsUiStrings,
    scrollBehavior: TopAppBarScrollBehavior,
    navigateBack: () -> Unit,
//    onResetPreferencesToDefaultClick: () -> Unit, // TODO: implement resetting all dev options
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
//        actions = {
//            IconButton(
//                onClick = onResetPreferencesToDefaultClick,
//            ) {
//                Icon(
//                    imageVector = ImageVector.vectorResource(DesignR.drawable.ic_restart_alt),
//                    contentDescription = strings.topBarResetPreferencesToDefaultIconDesc,
//                )
//            }
//        },
        colors = TopAppBarDefaults.topAppBarColors(),
        scrollBehavior = scrollBehavior,
    )
}

@Composable
fun DevOptions(
    strings: DevOptionsUiStrings,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp), // TODO: same style as on Settings screen. Extract into a reusable component?
    ) {
        item("predictable random colors") {
            PredictableRandomColors(
                title = strings.itemPredictableRandomColorsTitle,
                description = strings.itemPredictableRandomColorsDesc,
                value = "",
                onClick = {},
            )
        }
    }
}

@Preview
@Composable
private fun Preview() {
    TheColorTheme {
        DevOptionsScreen(
            navigateBack = {},
        )
    }
}