package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColorDetailsCrossfade(
    modifier: Modifier = Modifier,
    actualState: ColorDetailsState,
    animationSpec: FiniteAnimationSpec<Float> = ColorDetailsCrossfadeDefaults.animationSpec(),
    colorDetails: @Composable (state: ColorDetailsState) -> Unit,
) {
    val transition = updateTransition(
        targetState = actualState,
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