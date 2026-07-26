package io.github.mmolosay.thecolor.presentation.home.ui.preview

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The "bare" 'Color Preview' [Composable], free of any 'Home'-specific logic.
 */
internal typealias BareColorPreview =
        @Composable (
            animController: ColorPreviewAnimController,
            onUiStateReached: (reached: ColorPreviewUiState) -> Unit,
        ) -> Unit

/**
 * 'Color Preview' as the 'Home' feature presents it.
 */
@Composable
internal fun ColorPreviewInHome(
    flowOfData: StateFlow<ColorPreviewData?>,
    homeAnimController: HomeAnimController?,
    containerScrollState: ScrollState,
    stateOfContainerPosInRoot: State<Offset?>,
    colorPreview: BareColorPreview,
) {
    if (homeAnimController == null) return
    val coroutineScope = rememberCoroutineScope()
    val flowOfPositionAnimDest = run {
        val upstream = homeAnimController.flowOfDestState
        remember(upstream) {
            fun value(animState: HomeAnimState) = animState.colorPreviewPosition
            upstream
                .map(::value)
                .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), value(upstream.value))
        }
    }
    val flowOfVisibilityAnimDest = run {
        val upstream = homeAnimController.flowOfDestState
        remember(upstream) {
            fun value(animState: HomeAnimState) = animState.colorPreviewVisibility
            upstream
                .map(::value)
                .stateIn(coroutineScope, SharingStarted.WhileSubscribed(), value(upstream.value))
        }
    }
    val stateOfContainerViewportHeight =
        produceState<Int?>(initialValue = null, /*keys*/ containerScrollState.viewportSize) {
            value =
                containerScrollState.viewportSize.takeUnless { it == 0 } // consider 0 size as unknown
        }
    val animController = rememberColorPreviewAnimController(
        flowOfData = flowOfData,
        flowOfVisibilityAnimDest = flowOfVisibilityAnimDest,
    )

    if (animController != null) {
        DivingColorPreview(
            flowOfPositionAnimDest = flowOfPositionAnimDest,
            onPositionReached = homeAnimController::onValueReached,
            stateOfContainerViewportHeight = stateOfContainerViewportHeight,
            stateOfContainerPosInRoot = stateOfContainerPosInRoot,
        ) {
            @Suppress("MoveLambdaOutsideParentheses")
            colorPreview(
                animController,
                { reached -> homeAnimController.onValueReached(reached.toAnimState()) },
            )
        }
    }
}