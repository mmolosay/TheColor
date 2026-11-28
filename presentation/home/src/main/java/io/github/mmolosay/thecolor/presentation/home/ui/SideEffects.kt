package io.github.mmolosay.thecolor.presentation.home.ui

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeViewData.SessionColor
import io.github.mmolosay.thecolor.utils.cache.DequeCache
import io.github.mmolosay.thecolor.utils.cache.PruneOnSizeThreshold

@Composable
internal fun ProcessSessionColorAsSideEffect(
    sessionColor: SessionColor?,
) {
    val softwareKeyboardController = LocalSoftwareKeyboardController.current
    LaunchedEffect(sessionColor) {
        if (sessionColor == null) return@LaunchedEffect
        softwareKeyboardController?.hide()
    }
}

@Composable
internal fun ScrollToTopOnNullSessionColorAsSideEffect(
    sessionColor: SessionColor?,
    scrollState: ScrollState,
) {
    val cacheOfSessionColor = remember {
        DequeCache<SessionColor?>(
            mutationListener = PruneOnSizeThreshold(cacheSizeThreshold = 2),
        )
    }
    LaunchedEffect(sessionColor) {
        val current = sessionColor
        // previous may be present but equal to 'null'
        if (cacheOfSessionColor.isNotEmpty()) {
            val previous = cacheOfSessionColor.last()
            val hasSessionEnded = (previous != null && current == null)
            if (hasSessionEnded && scrollState.value != 0) {
                scrollState.animateScrollTo(0)
            }
        }
        cacheOfSessionColor += sessionColor
    }
}