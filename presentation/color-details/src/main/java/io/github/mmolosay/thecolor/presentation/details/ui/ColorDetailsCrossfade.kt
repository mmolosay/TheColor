package io.github.mmolosay.thecolor.presentation.details.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
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
    transition.AnimatedContent(
        modifier = modifier,
        transitionSpec = {
            val enter = fadeIn(animationSpec)
            val exit = fadeOut(animationSpec)
            val sizeTransform = SizeTransform(clip = false)
            (enter togetherWith exit) using sizeTransform
        },
    ) { facade ->
        colorDetails(facade)
    }
}

object ColorDetailsCrossfadeDefaults {

    fun animationSpec(): FiniteAnimationSpec<Float> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
        )
}