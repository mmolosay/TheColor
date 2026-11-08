package io.github.mmolosay.thecolor.presentation.scheme.ui

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
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeFeature
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeViewData

@Composable
fun ColorSchemeCrossfade(
    modifier: Modifier = Modifier,
    feature: ColorSchemeFeature,
    animationSpec: FiniteAnimationSpec<Float> = ColorSchemeCrossfadeDefaults.animationSpec(),
    colorScheme: @Composable (data: ColorSchemeViewData, feature: ColorSchemeFeature) -> Unit,
) {
    val actual by feature.viewData.collectAsStateWithLifecycle()
    val retained by retained(actual) { new ->
        if (new !is ColorSchemeViewData.Empty) {
            value = new
        }
    }
    val transition = updateTransition(
        targetState = retained,
        label = "Color Scheme cross-fade",
    )
    transition.AnimatedContent(
        modifier = modifier,
        transitionSpec = {
            val enter = fadeIn(animationSpec)
            val exit = fadeOut(animationSpec)
            val sizeTransform = SizeTransform(clip = false)
            (enter togetherWith exit) using sizeTransform
        },
        contentKey = { it::class }, // don't animate when only the values of the 'ViewData' change
    ) { data ->
        colorScheme(data, feature)
    }
}

object ColorSchemeCrossfadeDefaults {

    fun animationSpec(): FiniteAnimationSpec<Float> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
        )
}