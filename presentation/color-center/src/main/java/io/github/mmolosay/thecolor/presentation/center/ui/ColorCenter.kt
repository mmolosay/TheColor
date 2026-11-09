package io.github.mmolosay.thecolor.presentation.center.ui

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
import io.github.mmolosay.thecolor.presentation.center.viewmodel.ColorCenterFeature
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.PlaceholderDefaults
import io.github.mmolosay.thecolor.presentation.design.ProvideColorsOnTintedSurface
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.design.colorsOnLightSurface
import io.github.mmolosay.thecolor.presentation.details.ui.ColorDetails
import io.github.mmolosay.thecolor.presentation.details.ui.ColorDetailsCrossfade
import io.github.mmolosay.thecolor.presentation.scheme.ui.ColorScheme
import io.github.mmolosay.thecolor.presentation.scheme.ui.ColorSchemeCrossfade
import kotlinx.coroutines.launch
import kotlin.math.max
import io.github.mmolosay.thecolor.presentation.design.R as DesignR

@Composable
fun ColorCenter(
    feature: ColorCenterFeature,
    strings: ColorCenterUiStrings,
    modifier: Modifier = Modifier,
) {
    val crossfadeSpec = tween<Float>(
        durationMillis = 500,
        easing = FastOutSlowInEasing,
    )
    ColorCenter(
        modifier = modifier,
        strings = strings,
        page1Content = {
            ColorDetailsCrossfade(
                feature = feature.details,
                animationSpec = crossfadeSpec,
            ) { data, feature ->
                ColorDetails(
                    data = data,
                    feature = feature
                )
            }
        },
        page2Content = {
            ColorSchemeCrossfade(
                feature = feature.scheme,
                animationSpec = crossfadeSpec,
            ) { data, feature ->
                ColorScheme(
                    data = data,
                    feature = feature,
                )
            }
        },
    )
}

@Composable
fun ColorCenter(
    strings: ColorCenterUiStrings,
    page1Content: @Composable () -> Unit,
    page2Content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val density = LocalDensity.current
    val pagerState = rememberPagerState(
        pageCount = { 2 },
    )
    val coroutineScope = rememberCoroutineScope()
    var pageChangesInProgress by remember { mutableIntStateOf(0) }
    var minHeight by remember { mutableStateOf<Int?>(null) }
    val minHeightDp = with(density) { minHeight?.toDp() }

    fun changePage(pageIndex: Int) {
        coroutineScope.launch {
            pageChangesInProgress++
            try {
                pagerState.animateScrollToPage(page = pageIndex)
            } finally {
                pageChangesInProgress--
            }
        }
    }

    @Composable
    fun Page1() {
        Page(
            content = page1Content,
            changePageButton = {
                ChangePageButton(
                    text = strings.page1ChangePageButtonText,
                    onClick = { changePage(pageIndex = 1) },
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
                    onClick = { changePage(pageIndex = 0) },
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
        userScrollEnabled = (pageChangesInProgress == 0),
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
                    strings = previewUiStrings(),
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

private fun previewUiStrings() =
    ColorCenterUiStrings(
        page1ChangePageButtonText = "View color scheme",
        page2ChangePageButtonText = "View color details",
    )