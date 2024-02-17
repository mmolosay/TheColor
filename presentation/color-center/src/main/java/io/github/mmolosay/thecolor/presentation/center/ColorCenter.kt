package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface

@Composable
fun ColorCenter(
    vm: ColorCenterViewModel,
    details: @Composable () -> Unit,
    scheme: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val viewData = rememberViewData()
    val data = vm.dataFlow.collectAsStateWithLifecycle().value
    val uiData = ColorCenterUiData(data, viewData)
    ColorCenter(
        uiData = uiData,
        details = details,
        scheme = scheme,
        modifier = modifier,
    )
}

@Composable
fun ColorCenter(
    uiData: ColorCenterUiData,
    details: @Composable () -> Unit,
    scheme: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    ColorCenter(
        {
            DetailsPage(
                content = details,
                uiData = uiData.detailsPage,
            )
        },
        {
            SchemePage(
                content = scheme,
                uiData = uiData.schemePage,
            )
        },
        pageIndex = uiData.pageIndex,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorCenter(
    vararg pages: @Composable () -> Unit,
    pageIndex: Int,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(
        initialPage = pageIndex,
        pageCount = { pages.size },
    )
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        beyondBoundsPageCount = pages.size, // keep all pages loaded to keep height of Pager constant, TODO: solve with SubcomposeLayout?
    ) { pageIndex ->
        val page = pages[pageIndex]
        page()
    }

    LaunchedEffect(pageIndex) {
        pagerState.animateScrollToPage(page = pageIndex)
    }
}

@Composable
private fun rememberViewData(): ColorCenterUiData.ViewData {
    val context = LocalContext.current
    return remember { ColorCenterViewData(context) }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    @Composable
    fun Page(text: String) =
        Text(
            text = text,
            modifier = Modifier
                .background(Color.LightGray)
                .fillMaxWidth()
                .height(400.dp)
                .wrapContentSize(),
        )
    TheColorTheme {
        ProvideColorsOnTintedSurface(colors = colorsOnLightSurface()) {
            ColorCenter(
                uiData = previewUiData(),
                details = {
                    Page("Color details")
                },
                scheme = {
                    Page("Color scheme")
                },
            )
        }
    }
}

private fun previewUiData() =
    ColorCenterUiData(
        pageIndex = 0,
        detailsPage = ColorCenterUiData.Page(
            changePageButton = ColorCenterUiData.ChangePageButton(
                text = "View color scheme",
                onClick = {},
            ),
        ),
        schemePage = ColorCenterUiData.Page(
            changePageButton = ColorCenterUiData.ChangePageButton(
                text = "View color details",
                onClick = {},
            ),
        ),
    )