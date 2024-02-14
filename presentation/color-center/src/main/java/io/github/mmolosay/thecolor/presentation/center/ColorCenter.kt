package io.github.mmolosay.thecolor.presentation.center

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ColorCenter(
    vararg pages: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    HorizontalPager(
        state = pagerState,
        modifier = modifier,
        verticalAlignment = Alignment.Top,
        beyondBoundsPageCount = pages.size, // keep all pages loaded to keep height of Pager constant
    ) { pageIndex ->
        val page = pages[pageIndex]
        page()
    }
}

@Preview(showBackground = true)
@Composable
private fun Preview() {
    TheColorTheme {
        @Composable
        fun Page(text: String) =
            Text(
                text = text,
                modifier = Modifier
                    .fillMaxSize()
                    .wrapContentSize()
            )
        ColorCenter(
            { Page("Page #1") },
            { Page("Page #2") },
        )
    }
}