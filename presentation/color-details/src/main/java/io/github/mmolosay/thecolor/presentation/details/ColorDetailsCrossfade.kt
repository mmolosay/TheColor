package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewModel.DataState

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun ColorDetailsCrossfade(
    modifier: Modifier = Modifier,
    actualDataState: DataState,
    animationSpec: FiniteAnimationSpec<Float> = ColorDetailsCrossfadeDefaults.animationSpec(),
    colorDetails: @Composable (dataState: DataState) -> Unit,
) {
    val transition = updateTransition(
        targetState = actualDataState,
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