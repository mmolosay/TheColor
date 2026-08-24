package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsFacade

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColorDetailsCrossfade(
    modifier: Modifier = Modifier,
    actualFacade: ColorDetailsFacade,
    animationSpec: FiniteAnimationSpec<Float> = ColorDetailsCrossfadeDefaults.animationSpec(),
    colorDetails: @Composable (facade: ColorDetailsFacade) -> Unit,
) {
    val transition = updateTransition(
        targetState = actualFacade,
        label = "Color Details cross-fade",
    )
    transition.Crossfade(
        modifier = modifier,
        animationSpec = animationSpec,
        content = colorDetails,
    )
}

object ColorDetailsCrossfadeDefaults {

    fun animationSpec(): FiniteAnimationSpec<Float> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
        )
}