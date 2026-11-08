package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.vectorResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.center.ColorCenterData.SideEffect
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.PlaceholderDefaults
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.Job
import kotlin.math.max
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun rememberColorCenterFacade(handle: ColorCenterHandle): ColorCenterFacade {
    val data = handle.dataFlow.collectAsStateWithLifecycle().value
    return remember(handle, data) { handle.facade(data) }
}

@Composable
fun ColorCenter(
    facade: ColorCenterFacade,
    page1Content: @Composable () -> Unit,
    page2Content: @Composable () -> Unit,
    strings: ColorCenterUiStrings,
    modifier: Modifier = Modifier,
) {
    ColorCenter(
        data = facade.data,
        strings = strings,
        execute = facade.execute,
        page1Content = page1Content,
        page2Content = page2Content,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorCenter(
    data: ColorCenterData,
    strings: ColorCenterUiStrings,
    execute: ExecuteColorCenterAction,
    page1Content: @Composable () -> Unit,
    page2Content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val pagerState = rememberPagerState(
        pageCount = { 2 },
    )
    var userScrollEnabled by remember { mutableStateOf(true) }
    var minHeight by remember { mutableStateOf<Int?>(null) }
    val minHeightDp = with(density) { minHeight?.toDp() }

    @Composable
    fun Page1() {
        Page(
            content = page1Content,
            changePageButton = {
                ChangePageButton(
                    text = strings.page1ChangePageButtonText,
                    onClick = {
                        val action = ColorCenterAction.ChangePage(pageIndex = 1)
                        execute(action)
                    },
                    icon = ImageVector.vectorResource(DesignR.drawable.ic_keyboard_arrow_right),
                    iconPlacement = IconPlacement.Trailing,
                )
            },
        )
    }

    @Composable
    fun Page2() {
        Page(
            content = page2Content,
            changePageButton = {
                ChangePageButton(
                    text = strings.page2ChangePageButtonText,
                    onClick = {
                        val action = ColorCenterAction.ChangePage(pageIndex = 0)
                        execute(action)
                    },
                    icon = ImageVector.vectorResource(DesignR.drawable.ic_keyboard_arrow_left),
                    iconPlacement = IconPlacement.Leading,
                )
            },
        )
    }

    HorizontalPager(
        state = pagerState,
        modifier = modifier
            .onSizeChanged { size ->
                // once page is changed and removed from composition, we want to prevent pager from
                // down-sizing (height-wise) and either stay with the same height, or grow for new, bigger page
                minHeight = max(size.height, minHeight ?: 0)
            },
        verticalAlignment = Alignment.Top,
        userScrollEnabled = userScrollEnabled,
        key = { index -> index }, // list of pages doesn't change
    ) { pageIndex ->
        Box(
            modifier = Modifier.sizeIn(minHeight = minHeightDp ?: Dp.Unspecified),
            propagateMinConstraints = true, // propagate min height also to page content
        ) {
            when (pageIndex) {
                0 -> Page1()
                1 -> Page2()
                else -> error("Unexpected page index. Have you forgotten to increase 'pageCount'?")
            }
        }
    }

    ProcessSideEffectsAsSideEffect(
        sideEffects = data.sideEffects,
        onProcessed = { se ->
            val action = ColorCenterAction.OnSideEffectProcessed(se)
            execute(action)
        },
        changePage = { se ->
            try {
                userScrollEnabled = false
                pagerState.animateScrollToPage(page = se.pageIndex)
            } finally {
                userScrollEnabled = true // ensure re-enabled if LaunchedEffect() is cancelled
            }
        },
    )
}

@Composable
private fun ProcessSideEffectsAsSideEffect(
    sideEffects: ImmutableList<SideEffect>,
    onProcessed: (SideEffect) -> Unit,
    changePage: suspend (SideEffect.ChangePage) -> Unit,
) {
    suspend fun process(se: SideEffect.ChangePage) {
        changePage(se)
        onProcessed(se)
    }
    for (se in sideEffects) {
        key(se.id) {
            LaunchedEffect(Unit) {
                when (se) {
                    is SideEffect.ChangePage -> process(se)
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    @Composable
    fun Page(text: String) =
        Placeholder(
            modifier = Modifier
                .fillMaxWidth()
                .height(400.dp),
            color = PlaceholderDefaults.adjustedColor(LocalContentColor.current),
        ) {
            Text(text)
        }
    TheColorTheme {
        Surface(
            color = Color(0xFF_1A803F),
        ) {
            ProvideColorsOnTintedSurface(colors = colorsOnLightSurface()) {
                ColorCenter(
                    data = previewData(),
                    strings = previewUiStrings(),
                    execute = { Job() },
                    page1Content = {
                        Page("Color details")
                    },
                    page2Content = {
                        Page("Color scheme")
                    },
                )
            }
        }
    }
}

private fun previewData() =
    ColorCenterData(
        sideEffects = persistentListOf(),
    )

private fun previewUiStrings() =
    ColorCenterUiStrings(
        page1ChangePageButtonText = "View color scheme",
        page2ChangePageButtonText = "View color details",
    )