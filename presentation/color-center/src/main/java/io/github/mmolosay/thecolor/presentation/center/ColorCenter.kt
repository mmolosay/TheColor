package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.api.nav.bar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.details.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.scheme.ColorScheme
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlin.math.max

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColorCenter(
    viewModel: ColorCenterViewModel,
    modifier: Modifier = Modifier,
    navBarAppearanceController: NavBarAppearanceController,
) {
    val crossfadeSpec = tween<Float>(
        durationMillis = 500,
        easing = FastOutSlowInEasing,
    )
    ColorCenter(
        modifier = modifier,
        viewModel = viewModel,
        details = {
            val viewModel = viewModel.colorDetailsViewModel
            ColorDetailsCrossfade(
                actualDataState = viewModel.dataStateFlow.collectAsStateWithLifecycle().value,
                animationSpec = crossfadeSpec,
            ) { state ->
                ColorDetails(state = state)
            }
        },
        scheme = {
            val viewModel = viewModel.colorSchemeViewModel
            val state = viewModel.dataStateFlow.collectAsStateWithLifecycle().value
            val transition = updateTransition(
                targetState = state,
                label = "color scheme cross-fade",
            )
            transition.Crossfade(
                animationSpec = crossfadeSpec,
                contentKey = { it::class }, // don't animate when 'DataState' type stays the same
            ) { state ->
                ColorScheme(
                    state = state,
                    viewModel = viewModel,
                    navBarAppearanceController = navBarAppearanceController,
                )
            }
        },
    )
}

@Composable
fun ColorCenter(
    viewModel: ColorCenterViewModel,
    details: @Composable () -> Unit,
    scheme: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val context = LocalContext.current
    val strings = remember(context) { ColorCenterUiStrings(context) }
    val data = viewModel.dataFlow.collectAsStateWithLifecycle().value
    val uiData = ColorCenterUiData(data, strings)
    ColorCenter(
        uiData = uiData,
        colorDetails = details,
        colorScheme = scheme,
        modifier = modifier,
    )
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorCenter(
    uiData: ColorCenterUiData,
    colorDetails: @Composable () -> Unit,
    colorScheme: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val pages: ImmutableList<@Composable () -> Unit> = remember {
        persistentListOf(
            {
                DetailsPage(
                    uiData = uiData.detailsPage,
                    colorDetails = colorDetails,
                )
            },
            {
                SchemePage(
                    uiData = uiData.schemePage,
                    colorScheme = colorScheme,
                )
            },
        )
    }
    val pagerState = rememberPagerState(
        pageCount = { pages.size },
    )
    var userScrollEnabled by remember { mutableStateOf(true) }
    var minHeight by remember { mutableStateOf<Int?>(null) }
    val minHeightDp = with(density) { minHeight?.toDp() }

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
    ) { i ->
        val page = pages[i]
        Box(
            modifier = Modifier
                .sizeIn(minHeight = minHeightDp ?: Dp.Unspecified),
            propagateMinConstraints = true, // propagate min height also to page content
        ) {
            page()
        }
    }

    LaunchedEffect(uiData.changePageEvent) {
        val event = uiData.changePageEvent ?: return@LaunchedEffect
        userScrollEnabled = false
        pagerState.animateScrollToPage(page = event.destPage)
        event.onConsumed()
        userScrollEnabled = true
    }
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
            val data = previewData()
            val strings = previewUiStrings()
            val uiData = ColorCenterUiData(data, strings)
            ColorCenter(
                uiData = uiData,
                colorDetails = {
                    Page("Color details")
                },
                colorScheme = {
                    Page("Color scheme")
                },
            )
        }
    }
}

private fun previewData() =
    ColorCenterData(
        changePage = {},
        changePageEvent = null,
    )

private fun previewUiStrings() =
    ColorCenterUiStrings(
        detailsPageChangePageButtonText = "View color scheme",
        schemePageChangePageButtonText = "View color details",
    )