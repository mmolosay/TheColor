package io.github.mmolosay.thecolor.presentation.home.ui.preview

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewAnimController
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewData
import io.github.mmolosay.thecolor.presentation.preview.ColorPreviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The "bare" 'Color Preview' [Composable], free of any 'Home'-specific logic.
 */
internal typealias BareColorPreviewComposable =
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
    colorPreview: BareColorPreviewComposable,
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

@Suppress("unused") // params of 'BareColorPreviewComposable' lambda
@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            ColorPreviewInHome(
                flowOfData = remember {
                    val value = ColorPreviewData(
                        color = ColorInt(0x1A803F),
                    )
                    MutableStateFlow(value)
                },
                homeAnimController = remember {
                    val currentState = HomeAnimState(
                        colorPreviewPosition = HomeAnimState.ColorPreview.Position.NotDived,
                        colorPreviewVisibility = HomeAnimState.ColorPreview.Visibility.Visible,
                        colorCenter = HomeAnimState.ColorCenter.Collapsed,
                    )
                    HomeAnimController(currentState)
                },
                containerScrollState = rememberScrollState(),
                stateOfContainerPosInRoot = remember { mutableStateOf(Offset.Zero) },
            ) { animController, onUiStateReached ->
                Placeholder(
                    modifier = Modifier.size(48.dp),
                ) {
                    Text(
                        text = "Bare Color Preview",
                        autoSize = TextAutoSize.StepBased(),
                    )
                }
            }
        }
    }
}