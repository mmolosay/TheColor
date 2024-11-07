package io.github.mmolosay.thecolor.presentation.details

import androidx.compose.animation.Crossfade
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.runtime.Composable
import io.github.mmolosay.thecolor.presentation.details.ColorDetailsViewModel.DataState

@OptIn(ExperimentalAnimationApi::class)
@Suppress("SpellCheckingInspection")
@Composable
fun ColorDetailsCrossfade(
    actualDataState: DataState,
    animationSpec: FiniteAnimationSpec<Float> = defaultAnimationSpec(),
    colorDetails: @Composable (dataState: DataState) -> Unit,
) {
    val transition = updateTransition(
        targetState = actualDataState,
        label = "Color Details cross-fade",
    )
    transition.Crossfade(
        animationSpec = animationSpec,
        contentKey = { it::class }, // don't animate when 'DataState' type stays the same
        content = colorDetails,
    )
}

private fun defaultAnimationSpec(): FiniteAnimationSpec<Float> =
    tween(
        durationMillis = 500,
        easing = FastOutSlowInEasing,
    )