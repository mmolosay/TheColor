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
import androidx.compose.ui.Modifier
import io.github.mmolosay.thecolor.presentation.scheme.viewmodel.ColorSchemeFacade

@Composable
fun ColorSchemeCrossfade(
    modifier: Modifier = Modifier,
    actualFacade: ColorSchemeFacade,
    animationSpec: FiniteAnimationSpec<Float> = ColorSchemeCrossfadeDefaults.animationSpec(),
    colorScheme: @Composable (facade: ColorSchemeFacade) -> Unit,
) {
    val transition = updateTransition(
        targetState = actualFacade,
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
        contentKey = { it.state::class }, // don't animate when 'ColorSchemeState' type stays the same but only its values change
    ) { facade ->
        colorScheme(facade)
    }
}

object ColorSchemeCrossfadeDefaults {

    fun animationSpec(): FiniteAnimationSpec<Float> =
        spring(
            stiffness = Spring.StiffnessMediumLow,
        )
}