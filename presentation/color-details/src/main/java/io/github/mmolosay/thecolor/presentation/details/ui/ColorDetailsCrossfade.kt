package io.github.mmolosay.thecolor.presentation.details.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.github.mmolosay.thecolor.presentation.common.compose.retained
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsFeature
import io.github.mmolosay.thecolor.presentation.details.viewmodel.ColorDetailsViewData

@Composable
fun ColorDetailsCrossfade(
    modifier: Modifier = Modifier,
    feature: ColorDetailsFeature,
    animationSpec: FiniteAnimationSpec<Float> = ColorDetailsCrossfadeDefaults.animationSpec(),
    colorDetails: @Composable (data: ColorDetailsViewData, feature: ColorDetailsFeature) -> Unit,
) {
    val actual by feature.viewData.collectAsStateWithLifecycle()
    val retained by retained(actual) { new ->
        if (new !is ColorDetailsViewData.Empty) {
            value = new
        }
    }
    val transition = updateTransition(
        targetState = retained,
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
    ) { data ->
        colorDetails(data, feature)
    }
}

object ColorDetailsCrossfadeDefaults {

    fun animationSpec(): FiniteAnimationSpec<Float> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
        )
}