package io.github.mmolosay.thecolor.presentation.home.ui.center

import androidx.compose.foundation.ScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.geometry.Offset
import io.github.mmolosay.thecolor.presentation.common.navbar.NavBarAppearanceController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimController
import io.github.mmolosay.thecolor.presentation.home.ui.HomeAnimState
import io.github.mmolosay.thecolor.presentation.home.viewmodel.HomeData
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * The "bare" 'Color Center' [Composable], free of any 'Home'-specific logic.
 */
typealias BareColorCenter = @Composable () -> Unit

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
    colorCenter: BareColorCenter?,
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