package io.github.mmolosay.thecolor.presentation.home.ui.center

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The "bare" 'Color Center' [Composable], free of any 'Home'-specific logic.
 */
internal typealias BareColorCenterComposable =
        @Composable () -> Unit

/**
 * 'Color Center' as the 'Home' feature presents it.
 */
@Composable
internal fun ColorCenterInHome(
    proceedResult: HomeData.ProceedResult?,
    homeAnimController: HomeAnimController?,
    navBarAppearanceController: NavBarAppearanceController,
    containerScrollState: ScrollState,
    stateOfContainerPosInRoot: State<Offset?>,
    colorCenter: BareColorCenterComposable?,
) {
    if (homeAnimController == null) return
    val coroutineScope = rememberCoroutineScope()
    val decoratedColorCenter = remember(colorCenter, proceedResult) {
        decoratedColorCenterComposable(
            proceededColorData = (proceedResult as? HomeData.ProceedResult.Success)?.colorData,
            navBarAppearanceController = navBarAppearanceController,
            containerScrollState = containerScrollState,
            stateOfContainerPosInRoot = stateOfContainerPosInRoot,
            colorCenter = colorCenter,
        )
    }
    CircularRevealColorCenter(
        flowOfAnimDest = run {
            val upstream = homeAnimController.flowOfDestState
            remember(upstream) {
                fun value(animState: HomeAnimState) = animState.colorCenter
                upstream
                    .map(::value)
                    .stateIn(
                        coroutineScope,
                        SharingStarted.WhileSubscribed(),
                        value(upstream.value)
                    )
            }
        },
        onReached = homeAnimController::onValueReached,
        containerScrollState = containerScrollState,
        content = decoratedColorCenter,
    )
}

@Preview(uiMode = Configuration.UI_MODE_TYPE_NORMAL)
@Preview(uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL)
@Composable
private fun Preview() {
    TheColorTheme {
        Surface {
            ColorCenterInHome(
                proceedResult = HomeData.ProceedResult.Success(
                    colorData = HomeData.ProceedResult.Success.ColorData(
                        color = ColorInt(0x1A803F),
                        isDark = true,
                    ),
                ),
                homeAnimController = remember {
                    val currentState = HomeAnimState(
                        colorPreviewPosition = HomeAnimState.ColorPreview.Position.Dived,
                        colorPreviewVisibility = HomeAnimState.ColorPreview.Visibility.Visible,
                        colorCenter = HomeAnimState.ColorCenter.Expanded,
                    )
                    HomeAnimController(currentState)
                },
                navBarAppearanceController = remember { RootNavBarAppearanceController() },
                containerScrollState = rememberScrollState(),
                stateOfContainerPosInRoot = remember { mutableStateOf(Offset.Zero) },
            ) {
                Placeholder(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(512.dp),
                    color = MaterialTheme.colorScheme.onSurface,
                ) {
                    Text("Bare Color Center")
                }
            }
        }
    }
}