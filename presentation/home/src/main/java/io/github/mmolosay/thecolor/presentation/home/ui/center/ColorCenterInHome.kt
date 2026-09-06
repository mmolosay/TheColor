package io.github.mmolosay.thecolor.presentation.home.ui.center

import android.content.res.Configuration
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import io.github.mmolosay.thecolor.presentation.common.colorint.ColorInt
import io.github.mmolosay.thecolor.presentation.common.compose.Placeholder
import io.github.mmolosay.thecolor.presentation.common.compose.PlaceholderDefaults
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.common.navbar.RootNavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.design.TheColorTheme
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import io.github.mmolosay.thecolor.utils.mapState

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
    content: BareColorCenterComposable?,
) {
    if (homeAnimController == null) return
    val decoratedColorCenter = remember(content, proceedResult) {
        decoratedColorCenterComposable(
            proceededColorData = (proceedResult as? HomeData.ProceedResult.Success)?.colorData,
            navBarAppearanceController = navBarAppearanceController,
            containerScrollState = containerScrollState,
            stateOfContainerPosInRoot = stateOfContainerPosInRoot,
            content = content,
        )
    }
    CircularRevealColorCenter(
        flowOfAnimDest = run {
            val upstream = homeAnimController.flowOfDestState
            remember(upstream) {
                upstream.mapState { it.colorCenter }
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
                    color = PlaceholderDefaults.adjustedColor(LocalContentColor.current),
                ) {
                    Text("Bare Color Center")
                }
            }
        }
    }
}